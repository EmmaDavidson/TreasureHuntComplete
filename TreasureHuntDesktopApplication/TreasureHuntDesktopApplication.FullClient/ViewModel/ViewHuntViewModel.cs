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

namespace TreasureHuntDesktopApplication.FullClient.ViewModel
{
    public class ViewHuntViewModel : ViewModelBase, IDataErrorInfo
    {
        #region Setup
        ITreasureHuntService serviceClient;
        public RelayCommand SaveQuestionCommand { get; private set; }
        public RelayCommand PrintQRCodesCommand { get; private set; }
        public RelayCommand BackCommand { get; private set; }
        public RelayCommand LeaderboardCommand { get; private set; }
        public RelayCommand LogoutCommand { get; private set; }
        private String myFileDirectory = "C:\\Users\\Emma\\Documents\\GitHub\\EmmaProject\\TreasureHuntDesktopApplication\\";

        public ViewHuntViewModel(ITreasureHuntService _serviceClient)
        {
            serviceClient = _serviceClient;
            SaveQuestionCommand = new RelayCommand(() => ExecuteSaveQuestionCommand(), () => IsValidNewQuestion());
            PrintQRCodesCommand = new RelayCommand(() => ExecutePrintQRCodesCommand(), () => IsValidListOfQuestions());
            BackCommand = new RelayCommand(() => ExecuteBackCommand());
            LeaderboardCommand = new RelayCommand(() => ExecuteLeaderboardCommand());
            LogoutCommand = new RelayCommand(() => ExecuteLogoutCommand());
            
             Messenger.Default.Register<SelectedHuntMessage>
             (

             this,
             (action) => ReceiveSelectedHuntMessage(action.CurrentHunt)

             );

             RefreshQuestions();
        }

        #endregion

        #region Received Message Methods
        private void ReceiveSelectedHuntMessage(hunt currentHunt)
        {
            this.currentTreasureHunt = currentHunt;
            RefreshQuestions();
        }
        #endregion

        #region Validation

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

        public bool IsValidNewQuestion()
        {
            foreach (string property in ValidatedProperties)
                if (GetValidationMessage(property) != null)
                    return false;

            return true;
        }

        public bool IsValidListOfQuestions()
        {
            if(Questions.Count() != 0)
            {
                return true;
            }
            return false;
        
        }

        public bool IsSingleQuestionSelected()
        {
            if (this.CurrentQuestion != null)
            {
                return true;
            }
            return false;
        }
        #endregion

        #region Refreshing Data

        //make internal
        private void RefreshQuestions()
        {
            if (this.currentTreasureHunt != null)
            {
                List<long> questionIds = this.serviceClient.GetHuntQuestions(this.currentTreasureHunt).ToList();
                List<question> listOfQuestionsFromHunt = new List<question>();

                using (var questionIdNumbers = questionIds.GetEnumerator())
                {
                    while (questionIdNumbers.MoveNext())
                    {
                        question currentQuestionInList = this.serviceClient.GetQuestion(questionIdNumbers.Current);
                        listOfQuestionsFromHunt.Add(currentQuestionInList);
                    }

                    Questions = listOfQuestionsFromHunt.AsEnumerable();
                }
            }
        }

        #endregion

        #region Variable getters and setters

        private IEnumerable<question> questions;
        public IEnumerable<question> Questions
        {
            get { return this.questions; }
            set {              
                this.questions = value;
                RaisePropertyChanged("Questions");
            }       
        }

        private hunt currentTreasureHunt;
        public hunt CurrentTreasureHunt
        {
            get { return this.currentTreasureHunt; }
            set {                
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

        #region Commands

        private void ExecuteSaveQuestionCommand()
        {
            if (!DoesQuestionAlreadyExist(NewQuestion))
            {
                String locationOfQrCodeImage = myFileDirectory + "QRCodes\\" + this.CurrentTreasureHunt.HuntId + " " + this.newQuestion + ".png";

                question brandNewQuestion = new question();
                brandNewQuestion.Question1 = this.newQuestion;
                brandNewQuestion.URL = locationOfQrCodeImage;
                long questionId = this.serviceClient.SaveQuestion(brandNewQuestion);

                SaveHuntQuestion(questionId);
                EncodeQRCode(locationOfQrCodeImage);

                this.NewQuestion = String.Empty;
            }
            else 
            {
                String messageBoxText = "This question already exists.";
                String caption = "Question Already Exists";
                MessageBoxResult box = MessageBox.Show(messageBoxText, caption);
                NewQuestion = String.Empty;
            }
        }

        private void SaveHuntQuestion(long questionId)
        {
            huntquestion newHuntQuestion = new huntquestion();
            newHuntQuestion.QuestionId = questionId;
            newHuntQuestion.HuntId = currentTreasureHunt.HuntId;
            serviceClient.SaveNewHuntQuestion(newHuntQuestion);
        }

        public void EncodeQRCode(String locationOfQrCodeImage)
        {
            //-http://www.youtube.com/watch?v=3CSifXK62Tk
            QRCodeEncoder encoder = new QRCodeEncoder();
            Bitmap generatedQrCodeImage = encoder.Encode(CurrentTreasureHunt.HuntId + " " + this.NewQuestion);
            generatedQrCodeImage.Save(locationOfQrCodeImage, ImageFormat.Jpeg);

            RefreshQuestions();
        }

        //make internal
        public void ExecutePrintQRCodesCommand()
        {
            NewQuestion = null;
            //-http://cathalscorner.blogspot.co.uk/2009/04/docx-version-1002-released.html
            String newDocumentFileLocation = myFileDirectory + "Documents\\" + this.currentTreasureHunt.HuntName + " QR Codes Sheet.docx";

            //if(File.Exists(myFileDirectory + "Documents\\" + this.currentTreasureHunt.HuntName + " QR Codes Sheet.docx")
            //{
            using (DocX documentOfQRCodes = DocX.Create(newDocumentFileLocation))
            {
                Novacode.Paragraph p = documentOfQRCodes.InsertParagraph(this.currentTreasureHunt.HuntName);
                Novacode.Paragraph space = documentOfQRCodes.InsertParagraph("");
                documentOfQRCodes.InsertParagraph();
                using (var currentQuestionQRCode = this.questions.GetEnumerator())
                {
                    while (currentQuestionQRCode.MoveNext())
                    {
                        if (currentQuestionQRCode.Current.URL != null && currentQuestionQRCode.Current.URL != "empty URL")
                        {
                            documentOfQRCodes.InsertParagraph(currentQuestionQRCode.Current.Question1);
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
            }

            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "PrintViewModel" });
            Messenger.Default.Send<PrintMessage>(new PrintMessage() { FileLocation = newDocumentFileLocation });
            Messenger.Default.Send<SelectedHuntMessage>(new SelectedHuntMessage() { CurrentHunt = this.currentTreasureHunt });

         }

        private void ExecuteBackCommand()
        {
            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "SearchHuntViewModel" });
            NewQuestion = null;
        }

        private void ExecuteLeaderboardCommand()
        {
            hunt currentHunt = CurrentTreasureHunt;
            Messenger.Default.Send<LeaderboardMessage>(new LeaderboardMessage() { CurrentHunt = currentHunt });
            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "LeaderboardViewModel" });
           
        }

        private void ExecuteLogoutCommand()
        {

            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "LoginViewModel" });
        }

        private bool DoesQuestionAlreadyExist(string newQuestion)
        {   //REFACTOR THIS - ONLY DO ONE SERVICE CALL AND HAVE IT RETURN THE WHOLE QUESTION INSTEAD OF THE ID
            //GetHuntQuestions
            List<long> listOfQuestions = serviceClient.GetHuntQuestions(this.currentTreasureHunt).ToList();

            using (var currentHuntQuestionIds = listOfQuestions.GetEnumerator())
            {
                while (currentHuntQuestionIds.MoveNext())
                {
                    question question = serviceClient.GetQuestion(currentHuntQuestionIds.Current);

                    if (String.Equals(question.Question1, newQuestion, StringComparison.OrdinalIgnoreCase))
                    {
                        return true;
                    }
                }
            }

            return false;        
        }
        
        #endregion

        #region ETC
        public void Dispose()
        {
            throw new NotImplementedException();
        }
        #endregion

        #region IDataErrorInfo
        //-http://codeblitz.wordpress.com/2009/05/08/wpf-validation-made-easy-with-idataerrorinfo/
        string IDataErrorInfo.Error
        {
            get
            {
                return null;
            }
        }

        //What properties I am validating.
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

        private String ValidateQuestion()
        {
            if (Validation.IsNullOrEmpty(NewQuestion))
            {
                return "Question cannot be empty!";
            }
            //-http://blog.magnusmontin.net/2013/08/26/data-validation-in-wpf/
            if (!Validation.IsValidCharacters(NewQuestion))
            {
                return "There are invalid characters";
            }
            if (!Validation.IsValidLength(NewQuestion, NewQuestionMaxLength, NewQuestionMinLength))
            {
                return "Question is an invalid length!";
            }
          
            return null;
        }
        #endregion
    }
}
