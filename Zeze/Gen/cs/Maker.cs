﻿using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Text;

namespace Zeze.Gen.cs
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
            string projectDir = Project.Name;
            string genDir = System.IO.Path.Combine(projectDir, "Gen");
            string srcDir = projectDir;

            if (System.IO.Directory.Exists(genDir))
                System.IO.Directory.Delete(genDir, true);

            foreach (Types.Bean bean in Project.AllBeans)
            {
                new BeanFormatter(bean).Make(genDir);
            }
            foreach (Types.BeanKey beanKey in Project.AllBeanKeys)
            {
                new BeanKeyFormatter(beanKey).Make(genDir);
            }
            foreach (Protocol protocol in Project.AllProtocols)
            {
                if (protocol is Rpc rpc)
                    new RpcFormatter(rpc).Make(genDir);
                else
                    new ProtocolFormatter(protocol).Make(genDir);
            }
            foreach (Module mod in Project.AllModules)
            {
                new ModuleFormatter(Project, mod, genDir, srcDir).Make();
            }
            foreach (Manager ma in Project.Managers.Values)
            {
                new ManagerFormatter(ma, genDir, srcDir).Make();
            }

            new App(Project, genDir, srcDir).Make();
        }

    }
}
