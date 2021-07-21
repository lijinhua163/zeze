﻿
using System;
using System.Threading.Tasks;
using Zeze.Transaction;

namespace Zeze.Util
{
    public class Task
    {
        private static readonly NLog.Logger logger = NLog.LogManager.GetCurrentClassLogger();

        public static void Call(Action action, string actionName)
        {
            try
            {
                action();
            }
            catch (Exception ex)
            {
                logger.Error(ex, actionName);
            }
        }

        public static System.Threading.Tasks.Task Run(Action action, string actionName)
        {
            return System.Threading.Tasks.Task.Run(() => Call(action, actionName));
        }

        public static void LogAndStatistics(int result, Net.Protocol p, bool IsRequestSaved)
        {
            var actionName = p.GetType().FullName;
            if (IsRequestSaved == false)
                actionName = actionName + ":Response";

            if (result != 0)
            {
                var logLevel = (null != p.Service.Zeze)
                    ? p.Service.Zeze.Config.ProcessReturnErrorLogLevel
                    : NLog.LogLevel.Info;
                var module = "";
                if (result > 0)
                    module = "@" + Net.Protocol.GetModuleId(result) + ":" + Net.Protocol.GetProtocolId(result);
                logger.Log(logLevel,
                    "Task {0} Return={1}{2} UserState={3}",
                    actionName, result, module, p.UserState);
            }
#if ENABLE_STATISTICS
            ProcedureStatistics.Instance.GetOrAdd(actionName).GetOrAdd(result).IncrementAndGet();
#endif
        }

        public static int Call(Func<int> func, Net.Protocol p,
            Action<Net.Protocol, int> actionWhenError = null)
        {
            bool IsRequestSaved = p.IsRequest; // 记住这个，以后可能会被改变。
            try
            {
                int result = func();
                if (result != 0 && IsRequestSaved)
                {
                    actionWhenError?.Invoke(p, result);
                }
                LogAndStatistics(result, p, IsRequestSaved);
                return result;
            }
            catch (Exception ex)
            {
                while (ex is AggregateException)
                    ex = ex.InnerException;

                var errorCode = ex is TaskCanceledException
                    ? Procedure.CancelExcption
                    : Procedure.Excption;

                if (IsRequestSaved)
                    actionWhenError?.Invoke(p, errorCode);

                LogAndStatistics(errorCode, p, IsRequestSaved);
                // 使用 InnerException
                logger.Error(ex, "Task {0} Exception UserState={1}",
                    p.GetType().FullName, p.UserState);
                return errorCode;
            }
        }

        public static System.Threading.Tasks.Task Run(
            Func<int> func,
            Zeze.Net.Protocol p,
            Action<Net.Protocol, int> actionWhenError = null)
        {
            return System.Threading.Tasks.Task.Run(() => Call(func, p, actionWhenError));
        }

        public static int Call(
            Procedure procdure,
            Net.Protocol from = null,
            Action<Net.Protocol, int> actionWhenError = null)
        {
            bool? isRequestSaved = from?.IsRequest;
            try
            {
                // 日志在Call里面记录。因为要支持嵌套。
                // 统计在Call里面实现。
                int result = procdure.Call();
                if (result != 0 && null != isRequestSaved && isRequestSaved.Value)
                {
                    actionWhenError?.Invoke(from, result);
                }
                return result;
            }
            catch (Exception ex)
            {
                // Procedure.Call处理了所有错误。应该不会到这里。除非内部错误。
                if (null != isRequestSaved && isRequestSaved.Value)
                {
                    actionWhenError?.Invoke(from, Procedure.Excption);
                }
                logger.Error(ex, procdure.ActionName);
                return Procedure.Excption;
            }
        }

        public static System.Threading.Tasks.Task Run(
            Procedure procdure,
            Net.Protocol from = null,
            Action<Net.Protocol, int> actionWhenError = null)
        {
            return System.Threading.Tasks.Task.Run(() => Call(procdure, from, actionWhenError));
        }
    }
}
