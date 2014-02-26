using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight.Messaging;
using Microsoft.Office.Interop.Word;
using Microsoft.Win32;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Xps.Packaging;
using TreasureHuntDesktopApplication.FullClient.Messages;
using TreasureHuntDesktopApplication.FullClient.TreasureHuntService;
using Word = Microsoft.Office.Interop.Word;

namespace TreasureHuntDesktopApplication.FullClient.ViewModel
{
    public class PrintViewModel : ViewModelBase
    {
        ITreasureHuntService serviceClient;
        public RelayCommand BackCommand { get; set; }

        public PrintViewModel(ITreasureHuntService _serviceClient)
        {
            serviceClient = _serviceClient;
            BackCommand = new RelayCommand(() => ExecuteBackQuestionCommand());

            Messenger.Default.Register<PrintMessage>
             (

             this,
             (action) => ReceivePrintMessage(action.FileLocation)
             );

            Messenger.Default.Register<SelectedHuntMessage>
             (

             this,
             (action) => ReceiveSelectedHuntMessage(action.CurrentHunt)
             );
        }


        private FixedDocumentSequence documentViewer;
        public FixedDocumentSequence DocumentViewer
        {
            get { return this.documentViewer; }
            set
            {
                this.documentViewer = value;
                RaisePropertyChanged("DocumentViewer");
            }
        }

        private hunt currentTreasureHunt;
        public hunt CurrentTreasureHunt
        {
            get { return this.currentTreasureHunt; }
            set
            {
                this.currentTreasureHunt = value;
                RaisePropertyChanged("CurrentTreasureHunt");

            }
        }

        private XpsDocument _xpsDocument;
        public XpsDocument xpsDocument
        {
            get { return this._xpsDocument; }
            set
            {
                this._xpsDocument = value;
                RaisePropertyChanged("xpsDocument");

            }
        }

        //-http://code.msdn.microsoft.com/office/CSVSTOViewWordInWPF-db347436
        private void ReceivePrintMessage(String fileLocation)
        {
            string convertedXpsDoc = string.Concat("C:\\Users\\Emma\\Documents\\GitHub\\EmmaProject\\TreasureHuntDesktopApplication\\tempDoc1.xps");

            xpsDocument = ConvertWordToXps(fileLocation, convertedXpsDoc);
            if (xpsDocument == null)
            {
                return;
            }

            DocumentViewer = xpsDocument.GetFixedDocumentSequence();
        }

        private void ReceiveSelectedHuntMessage(hunt currentHunt)
        {
            this.currentTreasureHunt = currentHunt;
        }

        //-http://code.msdn.microsoft.com/office/CSVSTOViewWordInWPF-db347436
        private XpsDocument ConvertWordToXps(string wordFilename, string xpsFilename)
        {
            Word.Application wordApp = new Microsoft.Office.Interop.Word.Application();
            try
            {
                wordApp.Documents.Open(wordFilename);
                wordApp.Application.Visible = false;
                wordApp.WindowState = WdWindowState.wdWindowStateMinimize;

                Document doc = wordApp.ActiveDocument;
                doc.SaveAs(xpsFilename, WdSaveFormat.wdFormatXPS);                

                XpsDocument xpsDocument = new XpsDocument(xpsFilename, FileAccess.Read);
                return xpsDocument;
            }
            catch (Exception ex)
            {
                return null;
            }
            finally
            {
                wordApp.Documents.Close();
                ((_Application)wordApp).Quit(WdSaveOptions.wdDoNotSaveChanges);            
            } 
           
        }

        private void ExecuteBackQuestionCommand()
        {
            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "ViewHuntViewModel" });
            Messenger.Default.Send<SelectedHuntMessage>(new SelectedHuntMessage() { CurrentHunt = this.currentTreasureHunt });

            DocumentViewer = null;
            xpsDocument.Close();
            //Delete the temporary file
            File.Delete("C:\\Users\\Emma\\Documents\\GitHub\\EmmaProject\\TreasureHuntDesktopApplication\\tempDoc1.xps");

        }
    }
}
