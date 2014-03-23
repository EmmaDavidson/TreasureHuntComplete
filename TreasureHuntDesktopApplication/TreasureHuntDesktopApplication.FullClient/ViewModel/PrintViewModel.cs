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

//----------------------------------------------------------
//<copyright>
//</copyright>
//----------------------------------------------------------

namespace TreasureHuntDesktopApplication.FullClient.ViewModel
{
    /// <Summary> This is the ViewModel associated with the PrintView and is responsible for creating and displaying
    /// a document of QR Codes to be printed for a particular treasure hunt. 
    /// See Disseration Section 2.4.1.6 </Summary>

    public class PrintViewModel : ViewModelBase
    {

        #region Setup

        #region Fields

        #region General global variables
        private ITreasureHuntService serviceClient;
        public RelayCommand BackCommand { get; set; }
        #endregion 

        #region Binding variables

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

        private bool popupDisplayed;
        public bool PopupDisplayed
        {
            get { return this.popupDisplayed; }
            set
            {
                this.popupDisplayed = value;
                RaisePropertyChanged("PopupDisplayed");
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
        #endregion 

        #endregion

        #region Constructor
        public PrintViewModel(ITreasureHuntService serviceClient)
        {
            this.serviceClient = serviceClient;
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

            PopupDisplayed = false;
        }
        #endregion 

        #region Received Messages

        /// <summary>
        /// Method used to receive an incoming SelectedHuntMessage to store the data related to the current  
        /// hunt being accessed in the application 
        /// </summary>
        /// <param name="currentHunt"></param>
        private void ReceiveSelectedHuntMessage(hunt currentHunt)
        {
            this.currentTreasureHunt = currentHunt;
        }

        //-http://code.msdn.microsoft.com/office/CSVSTOViewWordInWPF-db347436
        /// <summary>
        /// Method used to receive an incoming PrintMessage to grab the data indicating a file location and to use this in
        /// the creation of a document of QR Codes associated with a given treasure hunt.</summary>
        /// <param name="fileLocation"></param>
        private void ReceivePrintMessage(String fileLocation)
        {
            string convertedXpsDoc = string.Concat("C:\\Users\\Emma\\Documents\\GitHub\\EmmaProject\\TreasureHuntDesktopApplication\\tempDoc1.xps");

            xpsDocument = ConvertWordToXpsFormat(fileLocation, convertedXpsDoc);
            if (xpsDocument == null)
            {
                return;
            }

            DocumentViewer = xpsDocument.GetFixedDocumentSequence();
        }
        #endregion 
        #endregion

        #region Methods
     
        //-http://code.msdn.microsoft.com/office/CSVSTOViewWordInWPF-db347436
        /// <summary>
        /// Method that will convert the word document associated with the currently selected hunt into a format that
        /// can be displayed and interacted with on screen. 
        /// </summary>
        /// <param name="wordFilename"></param>
        /// <param name="xpsFilename"></param>
        /// <returns></returns>
        private XpsDocument ConvertWordToXpsFormat(string wordFilename, string xpsFilename)
        {
            PopupDisplayed = true;

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
                PopupDisplayed = false;
            }
        }

        /// <summary>
        /// Method that will navigate the administrator back to the ViewHuntView and close the current document. 
        /// </summary>
        private void ExecuteBackQuestionCommand()
        {
            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "ViewHuntViewModel" });
            Messenger.Default.Send<SelectedHuntMessage>(new SelectedHuntMessage() { CurrentHunt = this.currentTreasureHunt });

            DocumentViewer = null;
            xpsDocument.Close();
            //Delete the temporary file
            File.Delete("C:\\Users\\Emma\\Documents\\GitHub\\EmmaProject\\TreasureHuntDesktopApplication\\tempDoc1.xps");

        }
        #endregion
    }
}
