﻿using System;
using System.Threading;
using System.Collections.Generic;

using System.Runtime.Serialization.Formatters.Binary;
using System.IO;

namespace server
{
    class Program
    {
        static void Main(string[] args)
        {
            string srcDirWhenPostBuild = null;
            for (int i = 0; i < args.Length; ++i)
            {
                switch (args[i])
                {
                    case "-srcDirWhenPostBuild":
                        srcDirWhenPostBuild = args[++i];
                        break;
                }
            }
            //srcDirWhenPostBuild = "C:\\Users\\86139\\Desktop\\code\\zeze\\Zezex\\server";

            if (null != srcDirWhenPostBuild)
            {
                Zeze.Arch.Rpc.GenModule.Instance.SrcDirWhenPostBuild = srcDirWhenPostBuild;
                Game.App.Instance.Create();
                if (Zeze.Arch.Rpc.GenModule.Instance.HasNewGen)
                {
                    Console.WriteLine("ModuleRedirect HasNewGen. Please Rebuild Now.");
                }
                Game.App.Instance.Destroy();
                return;
            }

            Game.App.Instance.Start(args);
            try
            {
                while (true)
                {
                    Thread.Sleep(1000);
                }
            }
            finally
            {
                Game.App.Instance.Stop();
            }
        }
    }
}
