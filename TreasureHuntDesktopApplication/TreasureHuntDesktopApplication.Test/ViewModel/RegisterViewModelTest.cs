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
    [TestFixture]
    class RegisterViewModelTest
    {
        #region Setup
        public RegisterViewModel viewModel;
        Mock<ITreasureHuntService> serviceClient;

        public RegisterViewModelTest()
        {
            serviceClient = new Mock<ITreasureHuntService>();
            viewModel = new RegisterViewModel(serviceClient.Object);
        }
        #endregion

        #region Variables

        public String Name
        {
            get 
            { 
                return viewModel.Name; 
            }
            set
            {
                viewModel.Name = value;
            }
        }

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

        public String RetypedPassword
        {
            get 
            { 
                return viewModel.RetypedPassword;
            }
            set
            {
                viewModel.RetypedPassword = value;
            }
        }
        #endregion

        #region Validation Tests

        [Test]
        public void NameInvalidWhenNull()
        {
            String userName = null;
            Name = userName;
            Password = "password";
            RetypedPassword = "password";
            EmailAddress = "fakeemail@gmail.com";
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void NameInvalidWhenWhitespace()
        {
            String userName = String.Empty;
            Name = userName;
            Password = "password";
            RetypedPassword = "password";
            EmailAddress = "fakeemail@gmail.com";
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void NameInvalidWhenGreaterThanMaxLength()
        {
            String userName = "abcdefghijklmnopqrstidshvfsdhkfhdskjfjdhfkhdsjhfjhdshfdsjfdshfhdjhfkjsdhfhdu";
            Name = userName;
            Password = "password";
            RetypedPassword = "password";
            EmailAddress = "fakeemail@gmail.com";
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void NameInvalidWhenLessThanMinLength()
        {
            String userName = "A";
            Name = userName;
            Password = "password";
            RetypedPassword = "password";
            EmailAddress = "fakeemail@gmail.com";
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void NameInvalidWhenInvalidCharacters()
        {
            String userName = "existingUser!";
            Name = userName;
            Password = "password";
            RetypedPassword = "password";
            EmailAddress = "fakeemail@gmail.com";
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void EmailInvalidWhenNull()
        {
            String email = null;
            EmailAddress = email;
            Name = "existingUser";
            Password = "password";
            RetypedPassword = "password";
            
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void EmailInvalidWhenWhitespace()
        {
            String email = String.Empty;
            EmailAddress = email;
            Name = "fakeuser";
            Password = "password";
            RetypedPassword = "password";
            
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void EmailInvalidWhenGreaterThanMaxLength()
        {
            String email = "abcdefghijklmnopqrstidshvfsdhkfhdskjfjdhfkhdsjhfjhdshfdsjfdshfhdjhfkjsdhfhdu@email.com";
            EmailAddress = email;
            Name = "fakeuser";
            Password = "password";
            RetypedPassword = "password";
            
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void EmailInvalidWhenLessThanMinLength()
        {
            String email = "a@b.c";
            EmailAddress = email;
            Name = "fakeuser";
            Password = "password";
            RetypedPassword = "password";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void EmailInvalidWhenInvalidFormat()
        {
            String email = "existingUser.com";
            EmailAddress = email;
            Name = "userName";
            Password = "password";
            RetypedPassword = "password";
            
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void PasswordInvalidWhenNull()
        {
            String password = null;
            Name = "fakeuser";
            Password = password;
            RetypedPassword = "password";
            EmailAddress = "fakeemail@gmail.com";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void PasswordInvalidWhenWhitespace()
        {
            String password = String.Empty;
            Name = "fakeuser";
            Password = password;
            RetypedPassword = "password";
            EmailAddress = "fakeemail@gmail.com";
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void PasswordInvalidWhenGreaterThanMaxLength()
        {
            String password = "abcdefghijklmnopqrstidshvfsdhkfhdskjfjdhfkhdsjhfjhdshfdsjfdshfhdjhfkjsdhfhdu";
            Name = "fakeuser";
            Password = password;
            RetypedPassword = "password";
            EmailAddress = "fakeemail@gmail.com";
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void PasswordInvalidWhenLessThanMinLength()
        {
            String password = "a";
            Name = "fakeuser";
            Password = password;
            RetypedPassword = "password";
            EmailAddress = "fakeemail@gmail.com";
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void PasswordInvalidWhenInvalidCharacters()
        {
            String password = "fakePassword!";
            Name = "fakeName";
            Password = password;
            RetypedPassword = "password";
            EmailAddress = "fakeemail@gmail.com";
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void RetypedPasswordInvalidWhenNull()
        {
            String password = null;
            Name = "fakeuser";
            Password = password;
            RetypedPassword = "password";
            EmailAddress = "fakeemail@gmail.com";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void RetypedPasswordInvalidWhenWhitespace()
        {
            String password = String.Empty;
            Name = "fakeuser";
            Password = password;
            RetypedPassword = "password";
            EmailAddress = "fakeemail@gmail.com";
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void RetypedPasswordInvalidWhenInvalidCharacters()
        {
            String password = "fakePassword!";
            Name = "fakeName";
            Password = password;
            RetypedPassword = "password";
            EmailAddress = "fakeemail@gmail.com";
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void RetypedPasswordInvalidWhenDoesNotMatchPassword()
        {
            String password = "fakePassword";
            Name = "fakeName";
            Password = password;
            RetypedPassword = "myFakePassword";
            EmailAddress = "fakeemail@gmail.com";
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        #endregion

        #region Command Tests

        //Problem with s => s.SaveUser(.newUser)
        [Test]
        public void ShouldRegisterUserIfNonExistingEmailAddress()
        {
            user newUser = new user();
            newUser.Email = "email@gmail.com";
            newUser.Password = "password";
            newUser.Name = "name";

            EmailAddress = newUser.Email;
            Password = newUser.Password;
            Name = newUser.Name;

            user existingUser = new user();
            existingUser.Name = "myFakeName";
            existingUser.Password = "password";
            existingUser.Email = "fakeEmailAddress@gmail.com";

            List<user> listOfUsers = new List<user>();
            listOfUsers.Add(existingUser);

            serviceClient.Setup(s => s.GetExistingUsers()).Returns(listOfUsers.ToArray());
            serviceClient.Setup(s => s.SaveUser(newUser)).Returns(1);
            serviceClient.Setup(s => s.SaveUserRole(It.IsAny<userrole>())).Verifiable();

            viewModel.ExecuteRegisterUserCommand();

            serviceClient.Verify(s => s.GetExistingUsers(), Times.Exactly(1));
            serviceClient.Verify(s => s.SaveUser(newUser), Times.Exactly(1));
            serviceClient.Verify(s => s.SaveUserRole(It.IsAny<userrole>()), Times.Exactly(1));
        }

        [Test]
        public void ShouldNotRegisterUserIfAlreadyExistingEmailAddress()
        {
            user newUser = new user();
            newUser.Email = "email@gmail.com";
            newUser.Password = "password";
            newUser.Name = "name";

            EmailAddress = newUser.Email;
            Password = newUser.Password;
            Name = newUser.Name;

            user existingUser = new user();
            existingUser.Name = "myFakeName";
            existingUser.Password = "password";
            existingUser.Email = "email@gmail.com";

            List<user> listOfUsers = new List<user>();
            listOfUsers.Add(existingUser);

            serviceClient.Setup(s => s.GetExistingUsers()).Returns(listOfUsers.ToArray());
            serviceClient.Setup(s => s.SaveUser(newUser)).Returns(1);
            serviceClient.Setup(s => s.SaveUserRole(It.IsAny<userrole>())).Verifiable();

            viewModel.ExecuteRegisterUserCommand();

            serviceClient.Verify(s => s.GetExistingUsers(), Times.Exactly(1));
            serviceClient.Verify(s => s.SaveUser(newUser), Times.Exactly(0));
            serviceClient.Verify(s => s.SaveUserRole(It.IsAny<userrole>()), Times.Exactly(0));
        }
        #endregion
    }
}
