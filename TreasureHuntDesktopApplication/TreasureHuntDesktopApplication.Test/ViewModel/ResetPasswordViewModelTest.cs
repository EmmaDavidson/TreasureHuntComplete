using Moq;
using NUnit.Framework;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using TreasureHuntDesktopApplication.FullClient.TreasureHuntService;
using TreasureHuntDesktopApplication.FullClient.ViewModel;

namespace TreasureHuntDesktopApplication.Test.ViewModel
{

    //----------------------------------------------------------
    //<copyright>
    //Emma Davidson - Treasure Hunt 2013-3014 Final Year Project
    //</copyright>
    //----------------------------------------------------------
    [TestFixture]
    public class ResetPasswordViewModelTest
    {
        #region Setup
        ResetPasswordViewModel viewModel;
        Mock<ITreasureHuntService> serviceClient;
        user currentUser;

        public ResetPasswordViewModelTest()
        {
            serviceClient = new Mock<ITreasureHuntService>();
            viewModel = new ResetPasswordViewModel(serviceClient.Object);

            currentUser = new user();
            currentUser.UserId = 1;
            currentUser.Name = "Emma";
            currentUser.Password = "Password";

            usersecurityquestion currentUserSecurityDetails = new usersecurityquestion();
            currentUserSecurityDetails.Answer = "answer";
            currentUserSecurityDetails.SecurityQuestionId = 1;
            currentUserSecurityDetails.UserId = 1;
            currentUserSecurityDetails.UserSecurityId = 1;

            securityquestion currentSecurityQuestion = new securityquestion();
            currentSecurityQuestion.SecurityQuestionId = 1;

            CurrentSecurityQuestion = currentSecurityQuestion;
            SecurityAnswer = currentUserSecurityDetails;
            CurrentUser = currentUser;  
        }

        #region Binding variables

        public user CurrentUser
        {
            get { return viewModel.CurrentUser; }
            set
            {

                viewModel.CurrentUser = value;
            }
        }

        public String NewPassword
        {
            get
            {
                return viewModel.NewPassword;
            }
            set
            {
                viewModel.NewPassword = value;
            }
        }

        public String UserSubmittedAnswer
        {
            get { return viewModel.UserSubmittedAnswer; }
            set
            {
                viewModel.UserSubmittedAnswer = value;
            }
        }

        public usersecurityquestion SecurityAnswer
        {
            get { return viewModel.SecurityAnswer; }
            set
            {
                viewModel.SecurityAnswer = value;
            }
        }

        public securityquestion CurrentSecurityQuestion
        {
            get { return viewModel.CurrentSecurityQuestion; }
            set
            {
                viewModel.CurrentSecurityQuestion = value;
            }
        }
        #endregion

        #endregion 

        #region Validation tests
        [Test]
        public void PasswordInvalidWhenNull()
        {
            String password = null;
            NewPassword = password;
            UserSubmittedAnswer = "answer";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.ResetPasswordCommand.CanExecute(""));
        }

        [Test]
        public void PasswordInvalidWhenWhitespace()
        {
            String password = String.Empty;
            NewPassword = password;
            UserSubmittedAnswer = "answer";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.ResetPasswordCommand.CanExecute(""));
        }

        [Test]
        public void PasswordInvalidWhenGreaterThanMaxLength()
        {
            String password = "abcdefghijklmnopqrstidshvfsdhkfhdskjfjdhfkhdsjhfjhdshfdsjfdshfhdjhfkjsdhfhdu";
            NewPassword = password;
            UserSubmittedAnswer = "answer";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.ResetPasswordCommand.CanExecute(""));
        }

        [Test]
        public void PasswordInvalidWhenLessThanMinLength()
        {
            String password = "p";
            NewPassword = password;
            UserSubmittedAnswer = "answer";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.ResetPasswordCommand.CanExecute(""));
        }

        [Test]
        public void PasswordInvalidWhenInvalidCharacters()
        {
            String password = "fakePassword!";
            NewPassword = password;
            UserSubmittedAnswer = "answer";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.ResetPasswordCommand.CanExecute(""));
        }

        [Test]
        public void AnswerInvalidWhenNull()
        {
            NewPassword = "password";
            UserSubmittedAnswer = null;

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.ResetPasswordCommand.CanExecute(""));
        }

        [Test]
        public void AnswerInvalidWhenWhitespace()
        {
            NewPassword = "password";
            UserSubmittedAnswer = String.Empty;

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.ResetPasswordCommand.CanExecute(""));
        }

        [Test]
        public void AnswerInvalidWhenItDoesNotMatchSecurityAnswerForGivenUser()
        {
            NewPassword = "password";
            UserSubmittedAnswer = "incorrect answer";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.ResetPasswordCommand.CanExecute(""));
        }
   
        #endregion

        #region Command tests

        [Test]
        public void ShouldSaveNewPasswordToDatabaseWhenValidPasswordSubmitted()
        {

            #region Setup
            NewPassword = "NewPass";
            UserSubmittedAnswer = "answer";
            #endregion

            serviceClient.Setup(s => s.updateUserPasswordAsync(It.IsAny<user>(), It.IsAny<string>())).Returns(Task.FromResult(""));

            viewModel.ExecutePasswordResetCommand();

            serviceClient.Verify(s => s.updateUserPasswordAsync(It.IsAny<user>(), It.IsAny<string>()), Times.Exactly(1));
        }

        #endregion


    }
}
