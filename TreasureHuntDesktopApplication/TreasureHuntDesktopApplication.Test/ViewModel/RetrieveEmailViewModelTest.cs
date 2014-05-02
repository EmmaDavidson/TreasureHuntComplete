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
    public class RetrieveEmailViewModelTest
    {
        #region Setup
        RetrieveEmailViewModel viewModel;
        Mock<ITreasureHuntService> serviceClient;

        public RetrieveEmailViewModelTest()
        {
            serviceClient = new Mock<ITreasureHuntService>();
            viewModel = new RetrieveEmailViewModel(serviceClient.Object);
        }

        public String EmailAddress
        {
            get { return viewModel.EmailAddress; }
            set
            {
                viewModel.EmailAddress = value;
            }
        }
        #endregion

        #region Validation tests

        [Test]
        public void  EmailAddressInvalidWhenNull()
        {
            EmailAddress = null;
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.CheckEmailAddressCommand.CanExecute(""));
        }

        [Test]
        public void EmailAddressInvalidWhenWhitespace()
        {
            EmailAddress = String.Empty;
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.CheckEmailAddressCommand.CanExecute(""));
        }

        [Test]
        public void EmailAddressInvalidWhenInvalidFormat()
        {
            EmailAddress = "emma@com";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.CheckEmailAddressCommand.CanExecute(""));
        }


        #endregion 

        #region Command tests

        //Will not work as won't all a null user to be returned
        [Test]
        public void ShouldPreventUserFromResettingPasswordIfUserDoesNotExist()
        {
            #region Setup
            user nullUser = new user();

            userrole newUserRole = new userrole();
            newUserRole.UserId = 1;
            newUserRole.RoleId = 1;
            #endregion

            serviceClient.Setup(s => s.GetUserAsync(It.IsAny<string>())).Returns(Task.FromResult(nullUser));
            serviceClient.Setup(s => s.GetUserRoleAsync(It.IsAny<user>())).Returns(Task.FromResult(newUserRole));

            viewModel.ExecuteCheckEmailAddressCommand();

            serviceClient.Verify(s => s.GetUserAsync(It.IsAny<string>()), Times.Exactly(1));
        }

        [Test]
        public void ShouldPreventUserFromResettingPasswordIfInvalidUserRole()
        {
            #region Setup
            user newUser = new user();
            newUser.UserId = 1;
            newUser.Name = "Emma";

            userrole newUserRole = new userrole();
            newUserRole.UserId = 1;
            newUserRole.RoleId = 2;
            #endregion

            serviceClient.Setup(s => s.GetUserAsync(It.IsAny<string>())).Returns(Task.FromResult(newUser));
            serviceClient.Setup(s => s.GetUserRoleAsync(It.IsAny<user>())).Returns(Task.FromResult(newUserRole));

            viewModel.ExecuteCheckEmailAddressCommand();

            serviceClient.Verify(s => s.GetUserAsync(It.IsAny<string>()), Times.Exactly(1));
            serviceClient.Verify(s => s.GetUserRoleAsync(It.IsAny<user>()), Times.Exactly(1));
        }

        #endregion 
    }
}
