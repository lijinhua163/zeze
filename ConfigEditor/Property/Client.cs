﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace ConfigEditor.Property
{
    public class Client : IProperty
    {
        public override string Name => "client";
        public override string Comment => "生成client发布数据时，包含此项数据";

        public override Group Group => Group.GenTarget;

        public override void VerifyCell(VerifyParam param)
        {
        }
    }
}