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
    public class ResetCompanyPasswordViewModelTest
    {
        #region Setup
        public ResetCompanyPasswordViewModel viewModel;
        Mock<ITreasureHuntService> serviceClient;
        user currentUser; 

        public ResetCompanyPasswordViewModelTest()
        {
            serviceClient = new Mock<ITreasureHuntService>();
            viewModel = new ResetCompanyPasswordViewModel(serviceClient.Object);

            currentUser = new user();
            currentUser.UserId = 1;
            currentUser.Name = "Emma";
            currentUser.Password = "Password";

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
        #endregion

        #endregion

        #region Validation tests

        [Test]
        public void PasswordInvalidWhenNull()
        {
            String password = null;
            NewPassword = password;

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.ResetCompanyPasswordCommand.CanExecute(""));
        }

        [Test]
        public void PasswordInvalidWhenWhitespace()
        {
            String password = String.Empty;
            NewPassword = password;

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.ResetCompanyPasswordCommand.CanExecute(""));
        }

        [Test]
        public void PasswordInvalidWhenGreaterThanMaxLength()
        {
            String password = "abcdefghijklmnopqrstidshvfsdhkfhdskjfjdhfkhdsjhfjhdshfdsjfdshfhdjhfkjsdhfhdu";
            NewPassword = password;

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.ResetCompanyPasswordCommand.CanExecute(""));
        }

        [Test]
        public void PasswordInvalidWhenLessThanMinLength()
        {
            String password = "p";
            NewPassword = password;

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.ResetCompanyPasswordCommand.CanExecute(""));
        }

        [Test]
        public void PasswordInvalidWhenInvalidCharacters()
        {
            String password = "fakePassword!";
            NewPassword = password;

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.ResetCompanyPasswordCommand.CanExecute(""));
        }

        #endregion

        #region Command tests

        [Test]
        public void ShouldSaveNewPasswordToDatabaseWhenValidPasswordSubmitted()
        {

            #region Setup
            NewPassword = "NewPassw";
            #endregion

            serviceClient.Setup(s => s.updateCompanyPasswordAsync(It.IsAny<user>(), It.IsAny<string>())).Returns(Task.FromResult(""));

            viewModel.ExecuteResetCompanyPasswordCommand();

            serviceClient.Verify(s => s.updateCompanyPasswordAsync(It.IsAny<user>(), It.IsAny<string>()), Times.Exactly(1));    
        }

        #endregion


    }
}
