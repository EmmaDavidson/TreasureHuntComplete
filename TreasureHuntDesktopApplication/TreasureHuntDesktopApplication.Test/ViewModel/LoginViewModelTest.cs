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
    class LoginViewModelTest
    {
        #region Setup
        public LoginViewModel viewModel;
        Mock<ITreasureHuntService> serviceClient;

        public LoginViewModelTest()
        {
            serviceClient = new Mock<ITreasureHuntService>();
            viewModel = new LoginViewModel(serviceClient.Object);
        }
        #endregion

        #region Variables
        public String Password
        {
            get 
            { 
                return viewModel.Password; 
            }
            set
            {
                viewModel.Password = value;
            }
        }


        public String EmailAddress
        {
            get
            { 
                return viewModel.EmailAddress; 
            }
            set
            {
                viewModel.EmailAddress = value;
            }
        }
        #endregion

        #region Validation Tests
        [Test]
        public void EmailInvalidWhenNull()
        {
            String email = null;
            EmailAddress = email;
            Password = "password";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.LoginUserCommand.CanExecute(""));
        }

        [Test]
        public void EmailInvalidWhenWhitespace()
        {
            String email = String.Empty;
            EmailAddress = email;
            Password = "password";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.LoginUserCommand.CanExecute(""));
        }

        [Test]
        public void EmailInvalidWhenGreaterThanMaxLength()
        {
            String email = "abcdefghijklmnopqrstidshvfsdhkfhdskjfjdhfkhdsjhfjhdshfdsjfdshfhdjhfkjsdhfhdu@email.com";
            EmailAddress = email;
            Password = "password";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.LoginUserCommand.CanExecute(""));
        }

        [Test]
        public void EmailInvalidWhenLessThanMinLength()
        {
            String email = "a@b.m";
            EmailAddress = email;
            Password = "password";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.LoginUserCommand.CanExecute(""));
        }

        [Test]
        public void EmailInvalidWhenInvalidFormat()
        {
            String email = "existingUser.com";
            EmailAddress = email;
            Password = "password";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.LoginUserCommand.CanExecute(""));
        }

        [Test]
        public void PasswordInvalidWhenNull()
        {
            String password = null;
            Password = password;
            EmailAddress = "fakeemail@gmail.com";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.LoginUserCommand.CanExecute(""));
        }

        [Test]
        public void PasswordInvalidWhenWhitespace()
        {
            String password = String.Empty;
            Password = password;
            EmailAddress = "fakeemail@gmail.com";
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.LoginUserCommand.CanExecute(""));
        }
        #endregion

        #region Command tests
        [Test]
        public void UserShouldBeLoggedInWhenValidLoginDetails()
        {
            EmailAddress = "user@gmail.com";
            Password = "password";

            user loginUser = new user();
            loginUser.Name = "userName";
            loginUser.Email = "user@gmail.com";
            loginUser.Password = "password";
            loginUser.UserId = 1;

            userrole newUserRole = new userrole();
            newUserRole.UserRoleId = 1;
            newUserRole.RoleId = 1;
            newUserRole.UserId = 1;

            serviceClient.Setup(s => s.GetUser(EmailAddress)).Returns(loginUser);
            serviceClient.Setup(s => s.GetUserRole(loginUser)).Returns(newUserRole);

            viewModel.ExecuteLoginUserCommand();

            serviceClient.Verify(s => s.GetUser(EmailAddress), Times.Exactly(1));
            serviceClient.Verify(s => s.GetUserRole(loginUser), Times.Exactly(1));
        }

        [Test]
        public void UserShouldNotBeLoggedInWhenInvalidLoginDetails()
        {
            EmailAddress = "user@gmail.com";
            Password = "wrongPassword";

            user loginUser = new user();
            loginUser.Name = "userName";
            loginUser.Email = "user@gmail.com";
            loginUser.Password = "password";
            loginUser.UserId = 1;

            userrole newUserRole = new userrole();
            newUserRole.UserRoleId = 1;
            newUserRole.RoleId = 1;
            newUserRole.UserId = 1;

            serviceClient.Setup(s => s.GetUser(EmailAddress)).Returns(loginUser);
            serviceClient.Setup(s => s.GetUserRole(loginUser)).Returns(newUserRole);

            viewModel.ExecuteLoginUserCommand();

            serviceClient.Verify(s => s.GetUser(EmailAddress), Times.Exactly(1));
            serviceClient.Verify(s => s.GetUserRole(loginUser), Times.Exactly(1));

            //Message box appears to say incorrect. More of a UI test
        }


        //This needs to be fixed to allow null to be passed in as an argument
        [Test]
        public void UserShouldNotBeLoggedInIfUserDoesNotExist()
        {
            EmailAddress = "user@gmail.com";
            Password = "password";

            user loginUser = new user();

            userrole newUserRole = new userrole();
            newUserRole.UserRoleId = 1;
            newUserRole.RoleId = 1;
            newUserRole.UserId = 1;

            //I want to be able to return null - this doesnt work
            serviceClient.Setup(s => s.GetUser(EmailAddress)).Returns(loginUser);
            serviceClient.Setup(s => s.GetUserRole(loginUser)).Returns(newUserRole);

            viewModel.ExecuteLoginUserCommand();

            serviceClient.Verify(s => s.GetUser(EmailAddress), Times.Exactly(1));
            serviceClient.Verify(s => s.GetUserRole(loginUser), Times.Exactly(0));
        }


        //More of a ui test
        [Test]
        public void UserShouldNotBeLoggedInIfWrongUserRole()
        {
            EmailAddress = "user@gmail.com";
            Password = "password";

            user loginUser = new user();
            loginUser.Name = "userName";
            loginUser.Email = "user@gmail.com";
            loginUser.Password = "password";
            loginUser.UserId = 1;

            userrole newUserRole = new userrole();
            newUserRole.UserRoleId = 1;
            newUserRole.RoleId = 2;
            newUserRole.UserId = 1;

            serviceClient.Setup(s => s.GetUser(EmailAddress)).Returns(loginUser);
            serviceClient.Setup(s => s.GetUserRole(loginUser)).Returns(newUserRole);

            viewModel.ExecuteLoginUserCommand();

            serviceClient.Verify(s => s.GetUser(EmailAddress), Times.Exactly(1));
            serviceClient.Verify(s => s.GetUserRole(loginUser), Times.Exactly(1));
        }

        #endregion
    }
}
