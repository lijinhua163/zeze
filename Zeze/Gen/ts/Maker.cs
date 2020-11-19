﻿using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Text;

namespace Zeze.Gen.ts
{
    public class Maker
    {
        public Project Project { get;  }

        public Maker(Project project)
        {
            Project = project;
        }

        public void Make()
        {
            string projectBasedir = Project.Gendir;
            string projectDir = System.IO.Path.Combine(projectBasedir, Project.Name);
            string genDir = Project.SrcDir.Length > 0 ? System.IO.Path.Combine(projectDir, Project.SrcDir) : projectDir;

            using System.IO.StreamWriter sw = Program.OpenWriterNoPath(genDir, "gen.ts");
            sw.WriteLine("// auto-generated");
            sw.WriteLine("");
            sw.WriteLine("import { Zeze } from \"zeze.js\"");
            sw.WriteLine("import Long from \"long.js\"");
            sw.WriteLine("");
            foreach (Types.Bean bean in Project.AllBeans)
            {
                new BeanFormatter(bean).Make(sw);
            }
            foreach (Types.BeanKey beanKey in Project.AllBeanKeys)
            {
                new BeanKeyFormatter(beanKey).Make(sw);
            }
            foreach (Protocol protocol in Project.AllProtocols)
            {
                if (protocol is Rpc rpc)
                {
                   new RpcFormatter(rpc).Make(sw);
                }
                else
                    new ProtocolFormatter(protocol).Make(sw);
            }
            foreach (Module mod in Project.AllModules)
            {
                new ModuleFormatter(Project, mod, genDir).Make();
            }
            new App(Project, genDir).Make();
            /*
            foreach (Service ma in Project.Services.Values)
            {
                new ServiceFormatter(ma, genDir, srcDir).Make();
            }
            */
        }

    }
}
