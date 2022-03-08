﻿using System.IO;
using Zeze.Gen.Types;

namespace Zeze.Gen.rrcs
{
    public class BeanFormatter
    {
        readonly Bean bean;

        public BeanFormatter(Bean bean)
        {
            this.bean = bean;
        }

        public void Make(string baseDir)
        {
            using StreamWriter sw = bean.Space.OpenWriter(baseDir, bean.Name + ".cs");

            sw.WriteLine("// auto-generated");
            sw.WriteLine("using ByteBuffer = Zeze.Serialize.ByteBuffer;");
            sw.WriteLine("using Environment = System.Environment;");
            //sw.WriteLine("using Zeze.Transaction.Collections;");
            sw.WriteLine();
            sw.WriteLine("namespace " + bean.Space.Path());
            sw.WriteLine("{");
            sw.WriteLine($"    public sealed class {bean.Name} : Zeze.Raft.RocksRaft.Bean");
            sw.WriteLine("    {");
            WriteDefine(sw);
            sw.WriteLine("    }");
            sw.WriteLine("}");
        }

        public void WriteDefine(StreamWriter sw)
        {
            // declare enums
            foreach (Enum e in bean.Enums)
                sw.WriteLine("        public const int " + e.Name + " = " + e.Value + ";" + e.Comment);
            if (bean.Enums.Count > 0)
                sw.WriteLine();

            // declare variables
            bean.Variables.Sort((a, b) => a.Id - b.Id);
            foreach (Variable v in bean.Variables)
            {
                Type vt = v.VariableType;
                string ro = vt is TypeCollection
                    || vt is TypeMap
                    || vt is Bean
                    || vt is TypeDynamic
                    ? "readonly " : "";
                sw.WriteLine("        " + ro + TypeName.GetName(vt) + " " + v.NamePrivate + ";" + v.Comment);
            }
            sw.WriteLine();

            Property.Make(bean, sw, "        ");
            Construct.Make(bean, sw, "        ");
            Assign.Make(bean, sw, "        ");
            // Copy
            sw.WriteLine("        public " + bean.Name + " CopyIfManaged()");
            sw.WriteLine("        {");
            sw.WriteLine("            return IsManaged ? Copy() : this;");
            sw.WriteLine("        }");
            sw.WriteLine();
            sw.WriteLine("        public " + bean.Name + " Copy()");
            sw.WriteLine("        {");
            sw.WriteLine("            var copy = new " + bean.Name + "();");
            sw.WriteLine("            copy.Assign(this);");
            sw.WriteLine("            return copy;");
            sw.WriteLine("        }");
            sw.WriteLine();
            sw.WriteLine($"        public static void Swap({bean.Name} a, {bean.Name} b)");
            sw.WriteLine("        {");
            sw.WriteLine($"            {bean.Name} save = a.Copy();");
            sw.WriteLine("            a.Assign(b);");
            sw.WriteLine("            b.Assign(save);");
            sw.WriteLine("        }");
            sw.WriteLine();
            sw.WriteLine("        public override Zeze.Raft.RocksRaft.Bean CopyBean()");
            sw.WriteLine("        {");
            sw.WriteLine("            return Copy();");
            sw.WriteLine("        }");
            sw.WriteLine();
            sw.WriteLine("        public const long TYPEID = " + bean.TypeId + ";");
            sw.WriteLine("        public override long TypeId => TYPEID;");
            sw.WriteLine();
            Tostring.Make(bean, sw, "        ");
            Encode.Make(bean, sw, "        ");
            Decode.Make(bean, sw, "        ");
            InitChildrenTableKey.Make(bean, sw, "        ");

            LeaderApplyNoRecursive.Make(bean, sw, "        ");
            FollowerApply.Make(bean, sw, "        ");
        }
    }
}