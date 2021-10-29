using System;
using System.Diagnostics;
using System.Runtime.Versioning;

namespace Zeze.Util
{
    public class Benchmark
    {
        private long startTime;
        private PerformanceCounter counter;

        [SupportedOSPlatform("windows")]
        private void StartPerformanceCounter()
        {
            counter = new PerformanceCounter(
                "Processor",
                "% Processor Time",
                "_Total",
                true);
        }

        [SupportedOSPlatform("windows")]
        private string ObtainProcessName()
        {
            string processName = null;
            bool notFound = true;
            int processOptionsChecked = 0;
            int maxNrOfParallelProcesses = 3 + 1;

            var baseProcessName = Process.GetCurrentProcess().ProcessName;
            var processId = Process.GetCurrentProcess().Id;
            while (notFound)
            {
                processName = baseProcessName;
                if (processOptionsChecked > maxNrOfParallelProcesses)
                {
                    break;
                }

                if (1 == processOptionsChecked)
                {
                    processName = string.Format("{0}_{1}", baseProcessName, processId);
                }
                else if (processOptionsChecked > 1)
                {
                    processName = string.Format("{0}#{1}", baseProcessName, processOptionsChecked - 1);
                }

                PerformanceCounter counter = new PerformanceCounter("Process", "ID Process", processName);
                if (processId == (int)counter.NextValue())
                {
                    notFound = !true;
                }
                processOptionsChecked++;
            }
            return processName;
        }

        public Benchmark()
        {
            if (OperatingSystem.IsWindows())
                StartPerformanceCounter();

            startTime = DateTime.Now.Ticks;
        }

        [SupportedOSPlatform("windows")]
        private float GetProcessCpuTime()
        {
            return counter.NextValue();
        }

        public void Report(String name, long tasks)
        {
            var cpu = OperatingSystem.IsWindows() ? GetProcessCpuTime() : 0.0f;
            var endTime = DateTime.Now.Ticks;
            var elapsedTime = endTime - startTime;
            var seconds = (double)elapsedTime / TimeSpan.TicksPerMillisecond / 1000;
            var concurrent = cpu / seconds;

            // format
            var stasks = string.Format("{0:0.00}", tasks / seconds);
            var stime = string.Format("{0:0.00}", seconds);
            var scpu = string.Format("{0:0.00}", cpu);
            var sconcurrent = string.Format("{0:0.00}", concurrent);
            Console.WriteLine($"{name} tasks/s={stasks} time={stime} cpu={scpu} concurrent={sconcurrent}");
        }
    }
}