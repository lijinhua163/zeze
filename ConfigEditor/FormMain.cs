﻿using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Text.Json;
using System.Windows.Forms;
using System.Xml;

namespace ConfigEditor
{
    public partial class FormMain : Form
    {
        public class EditorConfig
        {
            public IList<string> RecentHomes { get; set; }

            public string Home { get { return RecentHomes[0]; } }

            public void SetRecentHome(string home)
            {
                RecentHomes.Insert(0, home);
                IList<string> distinct = new List<string>();
                foreach (var r in RecentHomes.Distinct())
                    distinct.Add(r);
                RecentHomes = distinct;
                if (RecentHomes.Count > 10)
                    RecentHomes.RemoveAt(RecentHomes.Count - 1);
            }
        }

        public EditorConfig Config { get; private set; }
        public FormMain()
        {
            InitializeComponent();
            LoadConfig();
        }

        private string GetConfigFileFullName()
        {
            string localappdata = Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData);
            string confighome = System.IO.Path.Combine(localappdata, "zeze");
            System.IO.Directory.CreateDirectory(confighome);
            return System.IO.Path.Combine(confighome, "ConfigEditor.json");
        }

        private void LoadConfig()
        {
            try
            {
                string json = Encoding.UTF8.GetString(System.IO.File.ReadAllBytes(GetConfigFileFullName()));
                Config = JsonSerializer.Deserialize<EditorConfig>(json);
            }
            catch (Exception)
            {
                // skip error
            }
            if (null == Config)
                Config = new EditorConfig() { RecentHomes = new List<string>() };
        }

        private void SaveConfig()
        {
            var options = new JsonSerializerOptions { WriteIndented = true };
            System.IO.File.WriteAllBytes(GetConfigFileFullName(), JsonSerializer.SerializeToUtf8Bytes(Config, options));
        }

        private void FormMain_Load(object sender, EventArgs e)
        {
            this.folderBrowserDialog.Description = "选择配置所在的目录(Home)";
            while (Config.RecentHomes.Count > 0)
            {
                string first = Config.RecentHomes.First();
                if (System.IO.File.Exists(first))
                {
                    this.folderBrowserDialog.SelectedPath = first;
                    break;
                }
                Config.RecentHomes.RemoveAt(0);
            }
            this.folderBrowserDialog.ShowDialog();
            Config.SetRecentHome(this.folderBrowserDialog.SelectedPath);
            SaveConfig();
        }

        private TabPage NewTabPage(string text)
        {
            DataGridView grid = new DataGridView();
            grid.AllowUserToAddRows = false;
            grid.AllowUserToDeleteRows = false;
            grid.Anchor = ((AnchorStyles)((((AnchorStyles.Top | AnchorStyles.Bottom) | AnchorStyles.Left) | AnchorStyles.Right)));
            grid.ColumnHeadersHeightSizeMode = DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            grid.Location = new Point(0, 0);
            grid.Margin = new Padding(2);
            grid.MultiSelect = false;
            grid.Name = "Grid";
            grid.RowHeadersWidth = 82;
            grid.RowTemplate.Height = 37;
            //gird.Size = new Size(848, 476);
            grid.TabIndex = 0;

            TabPage tab = new TabPage();
            tab.Text = text;
            tab.Controls.Add(grid);
            return tab;
        }

        private void newButton_Click(object sender, EventArgs e)
        {
            tabs.Controls.Add(NewTabPage("NewFile"));

        }

        private Dictionary<string, Document> Documents = new Dictionary<string, Document>();

        private void save(TabPage tab)
        {
            try
            {
                if (tab == null)
                    return;

                System.Collections.IEnumerator ie = tab.Controls.GetEnumerator();
                if (!ie.MoveNext())
                    return;
                DataGridView grid = (DataGridView)tab.Controls.GetEnumerator().Current;

                Document doc = (Document)grid.Tag;
                if (null == doc)
                {
                    this.openFileDialog1.InitialDirectory = Config.RecentHomes[0];
                    this.openFileDialog1.FileName = "";
                    this.openFileDialog1.Filter = "(*.xml)|*.xml";
                    if (DialogResult.OK != this.openFileDialog1.ShowDialog())
                        return;
                    string file = this.openFileDialog1.FileName;
                    if (!file.EndsWith(".xml"))
                        file = file + ".xml";

                    doc = new Document(this, file);
                    Documents.Add(doc.RelateName, doc);
                    doc.Save();
                    doc.Xml.Save(file);
                    grid.Tag = doc;
                    doc.Grid = grid;
                    return;
                }
                doc.Save();
                doc.Xml.Save(doc.FileName);
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.ToString());
            }
        }

        private void saveButton_Click(object sender, EventArgs e)
        {
            save(tabs.SelectedTab);
        }

        private void openButton_Click(object sender, EventArgs e)
        {
            try
            {
                this.openFileDialog1.InitialDirectory = Config.RecentHomes[0];
                this.openFileDialog1.FileName = "";
                this.openFileDialog1.Filter = "(*.xml)|*.xml";
                if (DialogResult.OK != this.openFileDialog1.ShowDialog())
                    return;
                Document doc = new Document(this, this.openFileDialog1.FileName);
                if (Documents.TryGetValue(doc.RelateName, out var odoc))
                {
                    if (odoc.Grid != null)
                    {
                        // has opened
                        TabPage tab = (TabPage)odoc.Grid.Parent;
                        tab.Select();
                    }
                    else
                    {
                        // used by foreign, not opened
                        TabPage tab = NewTabPage(odoc.RelateName);
                        DataGridView grid = (DataGridView)tab.Controls[0];
                        odoc.Open();
                        tabs.Controls.Add(tab);

                        odoc.Grid = grid;
                        grid.Tag = doc;
                    }
                }
                else
                {
                    TabPage tab = NewTabPage(doc.RelateName);
                    DataGridView grid = (DataGridView)tab.Controls[0];
                    doc.Open();
                    Documents.Add(doc.RelateName, doc);
                    tabs.Controls.Add(tab);
                    grid.Tag = doc;
                    doc.Grid = grid;
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.ToString());
            }
        }

        private void saveAll()
        {
            System.Collections.IEnumerator ie = tabs.Controls.GetEnumerator();
            while (ie.MoveNext())
                save((TabPage)ie.Current);
        }

        private void saveAllButton_Click(object sender, EventArgs e)
        {
            saveAll();
        }

        private void FormMain_FormClosing(object sender, FormClosingEventArgs e)
        {
            saveAll();
        }

        private void buildButton_Click(object sender, EventArgs e)
        {
            saveAll();
            // TODO 遍历Home所有配置文件，并且生成代码等。
        }
    }
}
