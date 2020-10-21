﻿using System;
using System.Collections.Generic;
using System.Text;

namespace Zeze.Gen.lua
{
    public class ModuleFormatter
    {
        Project project;
        ModuleSpace module;
        string genDir;
        string srcDir;

        public ModuleFormatter(Project project, ModuleSpace module, string genDir, string srcDir)
        {
            this.project = project;
            this.module = module;
            this.genDir = genDir;
            this.srcDir = srcDir;
        }

        public void Make()
        {
            MakeGen();
            MakeSrc();
        }

        public void MakeGen()
        {
            if (null == module.Parent)
                return; // must be solution

            using System.IO.StreamWriter sw = module.Parent.OpenWriter(genDir, module.Name + ".lua");
            sw.WriteLine("-- auto-generated");
            sw.WriteLine();
            sw.WriteLine("local " + module.Name + " = {}");
            //sw.WriteLine("" + module.Name + ".ModuleId = " + module.Id);
            sw.WriteLine();
            foreach (var b in module.BeanKeys.Values)
            {
                BeanFormatter.Make(module.Name, b.Name, b.Variables, sw);
            }
            sw.WriteLine();
            foreach (var b in module.Beans.Values)
            {
                BeanFormatter.Make(module.Name, b.Name, b.Variables, sw);
            }
            sw.WriteLine();
            foreach (var p in module.Protocols.Values)
            {
                if (p is Rpc)
                    continue;
                ProtocolFormatter.Make(module.Name, p, sw);
            }
            sw.WriteLine();
            sw.WriteLine("return " + module.Name + "");
        }

        public void MakeSrc()
        {
            if (null == module.Parent)
                return; // must be solution

            using System.IO.StreamWriter sw = module.Parent.OpenWriter(srcDir, module.Name + ".lua", false);
            if (null == sw)
                return;

            sw.WriteLine($"local {module.Name} = {{}}");
            sw.WriteLine();
            sw.WriteLine($"function {module.Name}:Init()");
            Module realmod = (Module)module;
            Service serv = realmod.ReferenceService;
            if (serv != null)
            {
                int serviceHandleFlags = realmod.ReferenceService.HandleFlags;
                foreach (Protocol p in realmod.Protocols.Values)
                {
                    if (p is Rpc)
                        continue;

                    if (0 != (p.HandleFlags & serviceHandleFlags))
                    {
                        sw.WriteLine($"    -- TODO register protocol handle for '{p.Name}'");
                    }
                }
            }
            sw.WriteLine($"end");
            sw.WriteLine();

            if (serv != null)
            {
                int serviceHandleFlags = realmod.ReferenceService.HandleFlags;
                foreach (Protocol p in realmod.Protocols.Values)
                {
                    if (p is Rpc)
                        continue;

                    if (0 != (p.HandleFlags & serviceHandleFlags))
                    {
                        sw.WriteLine($"function {module.Name}:Process{p.Name}(p)");
                        sw.WriteLine($"    -- write handle here");
                        sw.WriteLine($"end");
                        sw.WriteLine($"");
                    }
                }
            }
        }
    }
}
