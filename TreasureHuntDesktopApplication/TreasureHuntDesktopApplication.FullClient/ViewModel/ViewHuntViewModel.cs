using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight.Messaging;
using MessagingToolkit.QRCode.Codec;
using MessagingToolkit.QRCode.Codec.Data;
using Novacode;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Drawing;
using System.Drawing.Imaging;
using System.Drawing.Printing;
using System.IO;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Windows;
using System.Windows.Documents;
using TreasureHuntDesktopApplication.FullClient.Messages;
using TreasureHuntDesktopApplication.FullClient.Project_Utilities;
using TreasureHuntDesktopApplication.FullClient.TreasureHuntService;
using TreasureHuntDesktopApplication.FullClient.Utilities;

//----------------------------------------------------------
//<copyright>
/*
 * Emma Davidson - Treasure Hunt 2013-3014 Final Year Project
 */
//</copyright>
//----------------------------------------------------------

namespace TreasureHuntDesktopApplication.FullClient.ViewModel
{
    /// <Summary> This is the ViewModel associated with the ViewHuntView and is responsible for the interaction
    /// between the View and the Model to display the data (such as the list of associated questions)
    /// which is associated with a particular treasure hunt. 
    /// See Dissertation Section 2.4.1.4 </Summary>

    public class ViewHuntViewModel : ViewModelBase, IDataErrorInfo
    {
        #region Setup

        #region Fields

        #region General global variables
        private ITreasureHuntService serviceClient;
        public RelayCommand SaveQuestionCommand { get; private set; }
        public RelayCommand PrintQRCodesCommand { get; private set; }
        public RelayCommand BackCommand { get; private set; }
        public RelayCommand LeaderboardCommand { get; private set; }
        private String myFileDirectory = "C:\\Users\\Emma\\Documents\\GitHub\\EmmaProject\\TreasureHuntDesktopApplication\\";

        private InternetConnectionChecker connectionChecker;
        #endregion

        #region Binding variables

        private IEnumerable<question> questions;
        public IEnumerable<question> Questions
        {
            get { return this.questions; }
            set
            {
                this.questions = value;
                RaisePropertyChanged("Questions");
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

        private String popupMessage;
        public String PopupMessage
        {
            get { return this.popupMessage; }
            set
            {
                this.popupMessage = value;
                RaisePropertyChanged("PopupMessage");
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

        public string newQuestion;
        public string NewQuestion
        {
            get { return this.newQuestion; }
            set
            {
                this.newQuestion = value;
                RaisePropertyChanged("NewQuestion");
            }
        }

        private question currentQuestion;
        public question CurrentQuestion
        {
            get { return this.currentQuestion; }
            set
            {

                this.currentQuestion = value;
                RaisePropertyChanged("CurrentQuestion");
            }
        }

        #endregion

        #region Validation variables

        public int NewQuestionMaxLength
        {
            get
            {
                return 150;

            }
        }

        public int NewQuestionMinLength
        {
            get
            {
                return 10;

            }
        }

        #endregion

        #endregion

        #region Constructor
        public ViewHuntViewModel(ITreasureHuntService serviceClient)
        {
            this.serviceClient = serviceClient;
            SaveQuestionCommand = new RelayCommand(() => ExecuteSaveQuestionCommand(), () => IsValidNewQuestion());
            PrintQRCodesCommand = new RelayCommand(() => ExecutePrintQRCodesCommand(), () => IsValidListOfQuestions());
            BackCommand = new RelayCommand(() => ExecuteBackCommand());
            LeaderboardCommand = new RelayCommand(() => ExecuteLeaderboardCommand());

            connectionChecker = InternetConnectionChecker.GetInstance();

            Messenger.Default.Register<SelectedHuntMessage>
            (

            this,
            (action) => ReceiveSelectedHuntMessage(action.CurrentHunt)

            );
        }
        #endregion 

        #region Received Messages

        /// <summary>
        /// Method used to receive an incoming SelectedHuntMessage to store the data related to the current  
        /// hunt being accessed. It will also refresh from the database a list of currently available questions for the 
        /// given hunt.
        /// </summary>
        /// <param name="currentHunt"></param>
        private void ReceiveSelectedHuntMessage(hunt currentHunt)
        {
            this.currentTreasureHunt = currentHunt;
            RefreshQuestions();
        }
        #endregion

        #endregion

        #region Methods

        #region General methods

        /// <summary>
        /// Method that attempts to pull down from the database a list of questions associated with the given
        /// treasure hunt. 
        /// </summary>
        public void RefreshQuestions()
        {
            if (connectionChecker.IsInternetConnected())
            {
                if (this.currentTreasureHunt != null)
                {
                    Questions = this.serviceClient.GetHuntQuestions(this.currentTreasureHunt).AsEnumerable();
                }
            }
            else
            {
                MessageBoxResult messageBox = MessageBox.Show(connectionChecker.ShowConnectionErrorMessage());
            }

            PopupDisplayed = false; 
        }

        /// <summary>
        /// Method that attempts to save a new question for the given treasure hunt.
        /// </summary>
        public async void ExecuteSaveQuestionCommand()
        {
            PopupMessage = "Saving...";
            PopupDisplayed = true;
            if (connectionChecker.IsInternetConnected())
            {
                //If the question does not already exist for this treasure hunt
                if (!DoesQuestionAlreadyExist(NewQuestion))
                {
                    //The location of where the new QR code image associated with this question will be stored.
                    String locationOfQrCodeImage = myFileDirectory + "QRCodes\\" + 
                        this.CurrentTreasureHunt.HuntId + " " + this.newQuestion + ".png";

                    //Then save the question to the database 
                    question brandNewQuestion = new question();
                    brandNewQuestion.Question1 = this.newQuestion;
                    brandNewQuestion.URL = locationOfQrCodeImage;
                    long questionId = await this.serviceClient.SaveQuestionAsync(brandNewQuestion);

                    SaveHuntQuestion(questionId);
                    EncodeQRCode(locationOfQrCodeImage);        
                }
                else
                {
                    PopupDisplayed = false;
                    String messageBoxText = "This question already exists!";
                    String caption = "Question Already Exists";
                    MessageBoxResult box = MessageBox.Show(messageBoxText, caption);
                    NewQuestion = String.Empty;
                }
            }
            else
            {
                MessageBoxResult messageBox = MessageBox.Show(connectionChecker.ShowConnectionErrorMessage());
            }

            
        }

        /// <summary>
        /// Method that saves the HuntQuestion data associated with the new question into the database.
        /// </summary>
        /// <param name="questionId"></param>
        private async void SaveHuntQuestion(long questionId)
        {
            huntquestion newHuntQuestion = new huntquestion();
            newHuntQuestion.QuestionId = questionId;
            newHuntQuestion.HuntId = currentTreasureHunt.HuntId;
            await serviceClient.SaveNewHuntQuestionAsync(newHuntQuestion);
            RefreshQuestions();
            this.NewQuestion = String.Empty;
        }

        /// <summary>
        /// Method that generates a new QR code that represents the new question saved.
        /// </summary>
        /// <param name="locationOfQrCodeImage"></param>
        public void EncodeQRCode(String locationOfQrCodeImage)
        {
            //-http://www.youtube.com/watch?v=3CSifXK62Tk
            QRCodeEncoder encoder = new QRCodeEncoder();
            Bitmap generatedQrCodeImage = encoder.Encode(CurrentTreasureHunt.HuntId + " " + this.NewQuestion);
            generatedQrCodeImage.Save(locationOfQrCodeImage, ImageFormat.Jpeg);
             
        }

        /// <summary>
        /// Method that creates a Microsoft Word Document full of the QR code images that are associated with each question
        /// of the current treasure hunt and passes to the Print view the location of where this new file is stored.
        /// </summary>
        public void ExecutePrintQRCodesCommand()
        {
            PopupMessage = "Preparing...";
            PopupDisplayed = true;
            NewQuestion = null;

            //-http://cathalscorner.blogspot.co.uk/2009/04/docx-version-1002-released.html
            
            //The location of where this word document will be stored.
            String newDocumentFileLocation = myFileDirectory + "Documents\\" + this.currentTreasureHunt.HuntName + " QR Codes Sheet.docx";

            //The creation of this new Word Document for the file location supplied
            using (DocX documentOfQRCodes = DocX.Create(newDocumentFileLocation))
            {
                Novacode.Paragraph p = documentOfQRCodes.InsertParagraph(this.currentTreasureHunt.HuntName);

                documentOfQRCodes.InsertParagraph();

                //For every question associated with the current treasure hunt
                using (var currentQuestionQRCode = this.questions.GetEnumerator())
                {
                    while (currentQuestionQRCode.MoveNext())
                    {
                        if (currentQuestionQRCode.Current.URL != null && currentQuestionQRCode.Current.URL != "empty URL")
                        {
                            //Insert into the document the QR associated with the current question 
                            documentOfQRCodes.InsertParagraph(currentQuestionQRCode.Current.Question1);
                            documentOfQRCodes.InsertParagraph();
                            Novacode.Paragraph q = documentOfQRCodes.InsertParagraph();

                            string locationOfImage = myFileDirectory + "QRCodes\\" + CurrentTreasureHunt.HuntId + " " + currentQuestionQRCode.Current.Question1 + ".png";
                            Novacode.Image img = documentOfQRCodes.AddImage(@locationOfImage);

                            Picture pic1 = img.CreatePicture();
                            q.InsertPicture(pic1, 0);
                            pic1.Width = 200;
                            pic1.Height = 200;

                            documentOfQRCodes.InsertParagraph();

                        }
                    }
                }

                documentOfQRCodes.Save();
                PopupDisplayed = false;
            }

            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "PrintViewModel" });
            Messenger.Default.Send<PrintMessage>(new PrintMessage() { FileLocation = newDocumentFileLocation });
            Messenger.Default.Send<SelectedHuntMessage>(new SelectedHuntMessage() { CurrentHunt = this.currentTreasureHunt });

        }

        /// <summary>
        /// Method that navigates the administrator back to the Homepage view.
        /// </summary>
        private void ExecuteBackCommand()
        {
            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "SearchHuntViewModel" });
            NewQuestion = null;
        }

        /// <summary>
        /// Method that navigates the administrator to the Leaderboard view associated with the current treasure hunt.
        /// </summary>
        private void ExecuteLeaderboardCommand()
        {
            if (connectionChecker.IsInternetConnected())
            {
                hunt currentHunt = CurrentTreasureHunt;
                Messenger.Default.Send<LeaderboardMessage>(new LeaderboardMessage() { CurrentHunt = currentHunt });
                Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "LeaderboardViewModel" });
            }
            else
            {
                MessageBoxResult messageBox = MessageBox.Show(connectionChecker.ShowConnectionErrorMessage());
            }
        }

        /// <summary>
        /// Method that attempts to check whether or not a given question already exists in the database for the given 
        /// treasure hunt. 
        /// </summary>
        /// <param name="newQuestion"></param>
        /// <returns></returns>
        private bool DoesQuestionAlreadyExist(string newQuestion)
        {   
            using (var currentHuntQuestion = Questions.GetEnumerator())
            {
                while (currentHuntQuestion.MoveNext())
                {
                    if (String.Equals(currentHuntQuestion.Current.Question1, newQuestion, StringComparison.OrdinalIgnoreCase))
                    {
                        return true;
                    }
                }
            }

            return false;
        }
        #endregion

        #region Validation

        /// <summary>
        /// Method to determine whether or not the new question submitted on screen is correct with 
        /// regards to its validation.
        /// </summary>
        /// <returns></returns>
        public bool IsValidNewQuestion()
        {
            foreach (string property in ValidatedProperties)
                if (GetValidationMessage(property) != null)
                    return false;

            return true;
        }

        /// <summary>
        /// Method to determine whether or not the list of questions available for the given treasure hunt is empty.
        /// </summary>
        /// <returns></returns>
        public bool IsValidListOfQuestions()
        {
            if (Questions.Count() != 0)
            {
                return true;
            }
            return false;

        }

        /// <summary>
        /// Method to determine if a question has been selected on screen.
        /// </summary>
        /// <returns></returns>
        public bool IsSingleQuestionSelected()
        {
            if (this.CurrentQuestion != null)
            {
                return true;
            }
            return false;
        }
        #endregion

        #region IDataErrorInfo validation methods
        //-http://codeblitz.wordpress.com/2009/05/08/wpf-validation-made-easy-with-idataerrorinfo/
        string IDataErrorInfo.Error
        {
            get
            {
                return null;
            }
        }

        //Properties to be validated
        static readonly string[] ValidatedProperties = 
        { 
            "NewQuestion"
        };

        string IDataErrorInfo.this[string propertyName]
        {
            get
            {
                return GetValidationMessage(propertyName);
            }
        }

        /// <summary>
        ///  Method that returns the validation message (if any) for a given property.
        /// </summary>
        /// <param name="propertyName"></param>
        /// <returns></returns>
        private string GetValidationMessage(string propertyName)
        {
            String result = null;

            switch (propertyName)
            {
                case "NewQuestion":
                    {
                        result = ValidateQuestion();
                        break;
                    }
            }

            return result;
        }

        /// <summary>
        /// Method that controls the validation of a given question
        /// </summary>
        /// <returns></returns>
        private String ValidateQuestion()
        {
            if (Validation.IsNullOrEmpty(NewQuestion))
            {
                return "Question cannot be empty.";
            }
            //-http://blog.magnusmontin.net/2013/08/26/data-validation-in-wpf/
            if (!Validation.IsValidCharacters(NewQuestion))
            {
                return "Question must be made up of only alphabetic characters.";
            }
            if (!Validation.IsValidLength(NewQuestion, NewQuestionMaxLength, NewQuestionMinLength))
            {
                return "Question must be between 10 and 150 characters.";
            }
            if (CurrentTreasureHunt.EndDate < DateTime.Today)
            {
                return "This treasure hunt is now out of date. You can no longer add a question.";
            }

            return null;
        }
        #endregion

        #endregion
    }
}
