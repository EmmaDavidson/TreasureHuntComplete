using Moq;
using NUnit.Framework;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using TreasureHuntDesktopApplication.FullClient.TreasureHuntService;
using TreasureHuntDesktopApplication.FullClient.ViewModel;

namespace TreasureHuntDesktopApplication.Test
{
    [TestFixture]
    class ViewHuntViewModelTest
    {
        #region Setup

        public ViewHuntViewModel viewModel;
        Mock<ITreasureHuntService> serviceClient;

        
        public const string newQuestion = "My new treasure hunt user";

        hunt myFakeHunt;
        question myFakeQuestion;

        List<hunt> returnedHunts = new List<hunt>();
        List<long> returnedIds = new List<long>();
        List<question> huntQuestions = new List<question>();

        long one = 111;

        [SetUp]
        public void Setup()
        {
            serviceClient = new Mock<ITreasureHuntService>();
            viewModel = new ViewHuntViewModel(serviceClient.Object);

            NewQuestion = newQuestion;

            myFakeHunt = new hunt();
            myFakeHunt.HuntName = "My Fake Hunt";
            myFakeQuestion = new question();
            myFakeQuestion.Question1 = "This is my user";
            myFakeQuestion.URL = "empty url";

            returnedHunts.Add(myFakeHunt);
            returnedIds.Add(one);

            CurrentTreasureHunt = myFakeHunt;
            CurrentQuestion = myFakeQuestion;

            huntQuestions.Add(myFakeQuestion);

            Questions = huntQuestions.AsEnumerable();
        }

        public String NewQuestion
        {
            get
            {
                return viewModel.NewQuestion;
            }
            set
            {
                viewModel.NewQuestion = value;
            }
        }

        public hunt CurrentTreasureHunt
        {
            get
            {
                return viewModel.CurrentTreasureHunt;
            }
            set
            {
                viewModel.CurrentTreasureHunt = value;
            }
        }



        public question CurrentQuestion
        {
            get
            {
                return viewModel.CurrentQuestion;
            }
            set
            {
                viewModel.CurrentQuestion = value;
            }
        }

        public IEnumerable<question> Questions
        {
            get 
            { 
                return viewModel.Questions; 
            }
            set
            {
                viewModel.Questions = value;
            }
        }
        #endregion

        #region Validation Tests
        [Test]
        public void NewQuestionInvalidWhenNull()
        {
            String nullNewQuestion = null;
            NewQuestion = nullNewQuestion;
            Assert.False(viewModel.IsValidNewQuestion());
            Assert.False(viewModel.SaveQuestionCommand.CanExecute(""));
        }

      [Test]
        public void NewQuestionInvalidWhenWhitespace()
        {
            String WhitespaceNewQuestion = String.Empty;
            NewQuestion = WhitespaceNewQuestion;
            Assert.False(viewModel.IsValidNewQuestion());
            Assert.False(viewModel.SaveQuestionCommand.CanExecute(""));
        }

        [Test]
        public void NewQuestionInValidWhenLessThanMinLength() 
        {
            String LongNewQuestion = "abcdefgh";
            NewQuestion = LongNewQuestion;
            Assert.False(viewModel.IsValidNewQuestion());
            Assert.False(viewModel.SaveQuestionCommand.CanExecute(""));
        }

        //Test for max length ommited as length too long to check
        #endregion

        #region Service Call Tests

        [Test] 
        public void ShouldRefreshQuestions()
        {
           List<question> listOfQuestions = new List<question>();
           listOfQuestions.Add(myFakeQuestion);

           this.CurrentTreasureHunt = myFakeHunt;
           serviceClient.Setup<long[]>(s => s.GetHuntQuestions(this.CurrentTreasureHunt)).Returns(returnedIds.ToArray());
           serviceClient.Setup<question>(s => s.GetQuestion(It.IsAny<long>())).Returns(myFakeQuestion);
           
           viewModel.SaveQuestionCommand.Execute(new Object());

           serviceClient.Verify(s => s.GetHuntQuestions(It.IsAny<hunt>()), Times.Exactly(2));
           serviceClient.Verify(s => s.GetQuestion(It.IsAny<long>()), Times.Exactly(2));

           Assert.AreEqual(listOfQuestions.ToArray(), this.Questions.ToArray()); 
        }

        [Test]
        public void ShouldSaveNewQuestion()
        {
           serviceClient.Setup<long>(s => s.SaveQuestion(It.IsAny<question>())).Returns(2); 
           serviceClient.Setup(s => s.SaveNewHuntQuestion(It.IsAny<huntquestion>())).Verifiable();

           viewModel.SaveQuestionCommand.Execute(new object());

           serviceClient.Verify(s => s.SaveQuestion(It.IsAny<question>()), Times.Exactly(1));
           serviceClient.Verify(s => s.SaveNewHuntQuestion(It.IsAny<huntquestion>()), Times.Exactly(1));
           
        }

        [Test]
        public void ShouldCreateTreasureHuntDocument()
        {
            viewModel.SaveQuestionCommand.Execute(new object());
            viewModel.ExecutePrintQRCodesCommand();
            String location = "C:\\Users\\Emma\\Documents\\GitHub\\EmmaProject\\TreasureHuntDesktopApplication\\Documents\\My Fake Hunt QR Codes Sheet.docx";
            //viewModel.PrintQRCodesCommand.Execute(new object());
            Assert.IsTrue(File.Exists(location), "The file does not exist");
        }

        [Test]
        public void ShouldCreateQRCodeImage()
        {   
            this.NewQuestion = "My new treasure hunt user";
            String location = "C:\\Users\\Emma\\Documents\\GitHub\\EmmaProject\\TreasureHuntDesktopApplication\\QRCodes\\My new treasure hunt user.png";
            viewModel.EncodeQRCode(location);
            Assert.IsTrue(File.Exists(location), "The file does not exist");
        }
        #endregion
    }
}

