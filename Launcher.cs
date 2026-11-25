using System;
using System.Diagnostics;
using System.IO;
using System.Windows.Forms;

namespace RouletteGameLauncher
{
    static class Program
    {
        [STAThread]
        static void Main()
        {
            try
            {
                string baseDir = AppDomain.CurrentDomain.BaseDirectory;
                string htmlPath = Path.Combine(baseDir, "index.html");

                if (!File.Exists(htmlPath))
                {
                    MessageBox.Show("No se encontró el archivo 'index.html' en la carpeta del juego.", 
                                    "Error al iniciar", MessageBoxButtons.OK, MessageBoxIcon.Error);
                    return;
                }

                string fileUri = new Uri(htmlPath).AbsoluteUri;

                // Candidate paths for Chromium-based browsers that support standalone app mode
                string[] browserPaths = new string[]
                {
                    @"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe",
                    @"C:\Program Files\Microsoft\Edge\Application\msedge.exe",
                    Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), @"Microsoft\Edge\Application\msedge.exe"),
                    @"C:\Program Files\Google\Chrome\Application\chrome.exe",
                    @"C:\Program Files (x86)\Google\Chrome\Application\chrome.exe",
                    Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), @"Google\Chrome\Application\chrome.exe")
                };

                string foundBrowser = null;
                foreach (string path in browserPaths)
                {
                    if (File.Exists(path))
                    {
                        foundBrowser = path;
                        break;
                    }
                }

                if (foundBrowser != null)
                {
                    ProcessStartInfo psi = new ProcessStartInfo();
                    psi.FileName = foundBrowser;
                    psi.Arguments = string.Format("--app=\"{0}\" --window-size=1040,920", fileUri);
                    psi.UseShellExecute = false;
                    Process.Start(psi);
                }
                else
                {
                    // Fallback to default browser
                    ProcessStartInfo psi = new ProcessStartInfo();
                    psi.FileName = fileUri;
                    psi.UseShellExecute = true;
                    Process.Start(psi);
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show("Ocurrió un error al iniciar el juego: " + ex.Message, 
                                "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }
    }
}
