﻿using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using Scriban;
using Zeze.Gen.Types;
using Zeze.Util;

namespace Zeze.Gen.luaClient
{
    public class Maker
    {
        Project Project { get; }

        public Maker(Project project)
        {
            Project = project;
        }

        static string GetTemplate(string fileName)
        {
            using var stream = Assembly.GetEntryAssembly()?.GetManifestResourceStream($"Gen.templates.{fileName}");
            if (stream == null) return "";
            TextReader tr = new StreamReader(stream);

            return tr.ReadToEnd();
        }
        public string FirstLetterToUpper(string str)
        {
            if (str == null)
                return null;

            if (str.Length > 1)
                return char.ToUpper(str[0]) + str.Substring(1);

            return str.ToUpper();
        }

        public void Make()
        {
            HashSet<ModuleSpace> allRefModules = new HashSet<ModuleSpace>();
            foreach (Module mod in Project.AllOrderDefineModules)
                allRefModules.Add(mod);

            foreach (BeanKey beanKey in Project.AllBeanKeys.Values)
            {
                allRefModules.Add(beanKey.Space);
            }

            foreach (Bean bean in Project.AllBeans.Values)
            {
                allRefModules.Add(bean.Space);
            }

            foreach (Protocol protocol in Project.AllProtocols.Values)
            {
                allRefModules.Add(protocol.Space);
            }

            List<ModuleSpace> allRefModulesList = new List<ModuleSpace>();
            foreach (ModuleSpace m in allRefModules)
            {
                var beans = Project.AllBeans.Values.Intersect(m.Beans.Values);
                var beanKeys = Project.AllBeanKeys.Values.Intersect(m.BeanKeys.Values);
                var protocols = Project.AllProtocols.Values.Intersect(m.Protocols.Values);
                if (!beans.Any() && !beanKeys.Any() && !protocols.Any())
                {
                    continue;
                }

                allRefModulesList.Add(m);
            }

            string projectBasedir = Project.GenDir;
            string projectDir = Path.Combine(projectBasedir, Project.Name);
            string metaDir = Path.Combine(projectDir, "msgmeta");
            string genDir = Path.Combine(projectDir, "msg");
            string srcDir = Path.Combine(projectDir, "module");
            Program.AddGenDir(genDir);
            {
                string luaMetaTemplateString = GetTemplate("LuaMeta.scriban-txt");
                Template template = Template.Parse(luaMetaTemplateString);
                string luaMeta = template.Render(new
                {
                    modules = allRefModulesList,
                    beans = Project.AllBeans.Values,
                    beankeys = Project.AllBeanKeys.Values,
                    protocols = Project.AllProtocols.Values
                });

                string metaFileName = Path.Combine(genDir, "ZezeMeta.lua");
                using StreamWriter swMeta = Program.OpenStreamWriter(metaFileName);
                swMeta.Write(luaMeta);
                swMeta.Close();
            }


            {
                string luaModuleTemplateString = GetTemplate("LuaModule.scriban-txt");
                Template moduleTemplate = Template.Parse(luaModuleTemplateString);
                foreach (var module in allRefModulesList)
                {
                    var beans = Project.AllBeans.Values.Intersect(module.Beans.Values).ToList();
                    var beanKeys = Project.AllBeanKeys.Values.Intersect(module.BeanKeys.Values).ToList();
                    var protocols = Project.AllProtocols.Values.Intersect(module.Protocols.Values).ToList();
                    if (!beans.Any() && !beanKeys.Any() && !protocols.Any())
                    {
                        continue;
                    }

                    string fullFileName = module.GetFullPath(genDir) + ".lua";
                    string fullDir = Path.GetDirectoryName(fullFileName);
                    string luaModule = moduleTemplate.Render(new
                    {
                        module,
                        beans,
                        beankeys = beanKeys,
                        protocols
                    });
                    if (fullDir != null) FileSystem.CreateDirectory(fullDir);
                    using var sw = Program.OpenStreamWriter(fullFileName);
                    sw.Write(luaModule);
                    sw.Close();
                }
            }

            {
                string luaModuleTemplateString = GetTemplate("LuaModuleMeta.scriban-txt");
                Template moduleTemplate = Template.Parse(luaModuleTemplateString);
                foreach (var module in allRefModulesList)
                {
                    var beans = Project.AllBeans.Values.Intersect(module.Beans.Values).ToList();
                    var beanKeys = Project.AllBeanKeys.Values.Intersect(module.BeanKeys.Values).ToList();
                    var protocols = Project.AllProtocols.Values.Intersect(module.Protocols.Values).ToList();
                    if (!beans.Any() && !beanKeys.Any() && !protocols.Any())
                    {
                        continue;
                    }

                    string fullFileName = module.GetFullPath(metaDir) + "Meta.lua";
                    string fullDir = Path.GetDirectoryName(fullFileName);
                    string luaModule = moduleTemplate.Render(new
                    {
                        module,
                        beans,
                        beankeys = beanKeys,
                        protocols
                    });
                    if (fullDir != null) FileSystem.CreateDirectory(fullDir);
                    using var sw = Program.OpenStreamWriter(fullFileName);
                    sw.Write(luaModule);
                    sw.Close();
                }
            }

            {
                string luaRootTemplateString = GetTemplate("LuaRoot.scriban-txt");
                Template rootTemplate = Template.Parse(luaRootTemplateString);
                string luaRoot = rootTemplate.Render(new
                {
                    modules = allRefModulesList,
                    solution = Project.Solution
                });

                using StreamWriter sw = Program.OpenStreamWriter(Path.Combine(genDir, "message.lua"));
                sw.Write(luaRoot);
                sw.Close();
            }

            {
                var solutions = allRefModulesList.Select(m => m.Solution).ToHashSet();
                string luaInitTemplateText = GetTemplate("message_init.lua");
                Template luaInitTemplate = Template.Parse(luaInitTemplateText);
                string luaRoot = luaInitTemplate.Render(new
                {
                    solutions
                });

                using StreamWriter sw = Program.OpenStreamWriter(Path.Combine(genDir, "message_init.lua"));
                sw.Write(luaRoot);
                sw.Close();
            }

            {
                string luaModuleTemplateString = GetTemplate("LuaModuleHandle.scriban-txt");
                Template moduleTemplate = Template.Parse(luaModuleTemplateString);
                FileChunkGen fileChunkGen = new FileChunkGen("--- [[ AUTO GENERATE START ]] ---",
                    "--- [[ AUTO GENERATE END ]] ---");
                foreach (ModuleSpace module in allRefModulesList)
                {
                    var protocols = Project.AllProtocols.Values.Intersect(module.Protocols.Values)
                        .Where(p => 0 != (p.HandleFlags & ((Module)module).ReferenceService.HandleFlags)).ToList();
                    if (!protocols.Any())
                    {
                        continue;
                    }

                    string fullDir = module.GetFullPath(srcDir);
                    string fullFileName = Path.Combine(fullDir, $"Module{FirstLetterToUpper(module.Name)}.lua");

                    if (!fileChunkGen.LoadFile(fullFileName))
                    {
                        string luaModule = moduleTemplate.Render(new
                        {
                            module, protocols
                        });
                        FileSystem.CreateDirectory(fullDir);
                        using var sw = Program.OpenStreamWriter(fullFileName);
                        sw.Write(luaModule);
                        sw.Close();
                        continue;
                    }

                    if (fileChunkGen.Chunks.Count < 3)
                    {
                        continue;
                    }
                    
                    var handlerChunk = fileChunkGen.Chunks[2];
                    var generatedHandlers = new HashSet<string>();


                    foreach (var line in handlerChunk.Lines)
                    {
                        if (line.StartsWith($"function {module.Name}.OnMsg_"))
                        {
                            var protoName = line.Substring($"function {module.Name}.OnMsg_".Length).Split("(")[0];
                            generatedHandlers.Add(protoName.Trim());
                        }
                    }

                    fileChunkGen.SaveFile(fullFileName, (writer, chunk) =>
                        {
                            writer.WriteLine($"function {module.Name}.RegisterHandlers()");
                            foreach (var protocol in protocols)
                            {
                                writer.WriteLine(
                                    $"    msg.{protocol.FullName}.Handle = {module.Name}.OnMsg_{protocol.Name}");
                            }

                            writer.WriteLine("end");
                        }, null, (writer, chunk) =>
                        {
                            foreach (var protocol in protocols)
                            {
                                if (generatedHandlers.Contains(protocol.Name))
                                    continue;
                                writer.WriteLine();
                                writer.WriteLine($"---@param p msg.{protocol.FullName}");
                                writer.WriteLine($"function {module.Name}.OnMsg_{protocol.Name}(p)");
                                writer.WriteLine("end");
                            }
                        }
                    );
                }
            }
        }
    }
}