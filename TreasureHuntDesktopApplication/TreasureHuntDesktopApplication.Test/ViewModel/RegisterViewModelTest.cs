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

        public securityquestion CurrentSecurityQuestion
        {
            get { return viewModel.CurrentSecurityQuestion; }
            set
            {
                viewModel.CurrentSecurityQuestion = value;
            }
        }

        public String CompanyName
        {
            get { return viewModel.CompanyName; }
            set
            {
                viewModel.CompanyName = value;
            }
        }

        public String CompanyPassword
        {
            get { return viewModel.CompanyPassword; }
            set
            {
                viewModel.CompanyPassword = value;
            }
        }

        public String SecurityAnswer
        {
            get { return viewModel.SecurityAnswer; }
            set
            {
                viewModel.SecurityAnswer = value;
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
            EmailAddress = "fakeemail@gmail.com";
            CurrentSecurityQuestion = new securityquestion();
            CurrentSecurityQuestion.SecurityQuestionId = 1;
            CompanyName = "New Company Name";
            CompanyPassword = "Password";
            SecurityAnswer = "answer";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void NameInvalidWhenWhitespace()
        {
            String userName = String.Empty;
            Name = userName;
            Password = "password";
            EmailAddress = "fakeemail@gmail.com";
            CurrentSecurityQuestion = new securityquestion();
            CurrentSecurityQuestion.SecurityQuestionId = 1;
            CompanyName = "New Company Name";
            CompanyPassword = "Password";
            SecurityAnswer = "answer";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void NameInvalidWhenGreaterThanMaxLength()
        {
            String userName = "abcdefghijklmnopqrstidshvfsdhkfhdskjfjdhfkhdsjhfjhdshfdsjfdshfhdjhfkjsdhfhdu";
            Name = userName;
            Password = "password";
            EmailAddress = "fakeemail@gmail.com";
            CurrentSecurityQuestion = new securityquestion();
            CurrentSecurityQuestion.SecurityQuestionId = 1;
            CompanyName = "New Company Name";
            CompanyPassword = "Password";
            SecurityAnswer = "answer";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void NameInvalidWhenLessThanMinLength()
        {
            String userName = "A";
            Name = userName;
            Password = "password";
            CurrentSecurityQuestion = new securityquestion();
            CurrentSecurityQuestion.SecurityQuestionId = 1;
            CompanyName = "New Company Name";
            CompanyPassword = "Password";
            SecurityAnswer = "answer";
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
            CurrentSecurityQuestion = new securityquestion();
            CurrentSecurityQuestion.SecurityQuestionId = 1;
            CompanyName = "New Company Name";
            CompanyPassword = "Password";
            SecurityAnswer = "answer";
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
            CurrentSecurityQuestion = new securityquestion();
            CurrentSecurityQuestion.SecurityQuestionId = 1;
            CompanyName = "New Company Name";
            CompanyPassword = "Password";
            SecurityAnswer = "answer";
            
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
            CurrentSecurityQuestion = new securityquestion();
            CurrentSecurityQuestion.SecurityQuestionId = 1;
            CompanyName = "New Company Name";
            CompanyPassword = "Password";
            SecurityAnswer = "answer";
            
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
            CurrentSecurityQuestion = new securityquestion();
            CurrentSecurityQuestion.SecurityQuestionId = 1;
            CompanyName = "New Company Name";
            CompanyPassword = "Password";
            SecurityAnswer = "answer";
            
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
            CurrentSecurityQuestion = new securityquestion();
            CurrentSecurityQuestion.SecurityQuestionId = 1;
            CompanyName = "New Company Name";
            CompanyPassword = "Password";
            SecurityAnswer = "answer";

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
            CurrentSecurityQuestion = new securityquestion();
            CurrentSecurityQuestion.SecurityQuestionId = 1;
            CompanyName = "New Company Name";
            CompanyPassword = "Password";
            SecurityAnswer = "answer";
            
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void PasswordInvalidWhenNull()
        {
            String password = null;
            Name = "fakeuser";
            Password = password;
            EmailAddress = "fakeemail@gmail.com";
            CurrentSecurityQuestion = new securityquestion();
            CurrentSecurityQuestion.SecurityQuestionId = 1;
            CompanyName = "New Company Name";
            CompanyPassword = "Password";
            SecurityAnswer = "answer";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void PasswordInvalidWhenWhitespace()
        {
            String password = String.Empty;
            Name = "fakeuser";
            Password = password;
            EmailAddress = "fakeemail@gmail.com";
            CurrentSecurityQuestion = new securityquestion();
            CurrentSecurityQuestion.SecurityQuestionId = 1;
            CompanyName = "New Company Name";
            CompanyPassword = "Password";
            SecurityAnswer = "answer";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void PasswordInvalidWhenGreaterThanMaxLength()
        {
            String password = "abcdefghijklmnopqrstidshvfsdhkfhdskjfjdhfkhdsjhfjhdshfdsjfdshfhdjhfkjsdhfhdu";
            Name = "fakeuser";
            Password = password;
            EmailAddress = "fakeemail@gmail.com";
            CurrentSecurityQuestion = new securityquestion();
            CurrentSecurityQuestion.SecurityQuestionId = 1;
            CompanyName = "New Company Name";
            CompanyPassword = "Password";
            SecurityAnswer = "answer";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void PasswordInvalidWhenLessThanMinLength()
        {
            String password = "a";
            Name = "fakeuser";
            Password = password;
            EmailAddress = "fakeemail@gmail.com";
            CurrentSecurityQuestion = new securityquestion();
            CurrentSecurityQuestion.SecurityQuestionId = 1;
            CompanyName = "New Company Name";
            CompanyPassword = "Password";
            SecurityAnswer = "answer";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void PasswordInvalidWhenInvalidCharacters()
        {
            String password = "fakePassword!";
            Name = "fakeName";
            Password = password;
            EmailAddress = "fakeemail@gmail.com";
            CurrentSecurityQuestion = new securityquestion();
            CurrentSecurityQuestion.SecurityQuestionId = 1;
            CompanyName = "New Company Name";
            CompanyPassword = "Password";
            SecurityAnswer = "answer";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void SecurityQuestionInvalidWhenNull()
        {
            String password = "fakePassword!";
            Name = "fakeName";
            Password = password;
            EmailAddress = "fakeemail@gmail.com";
            CurrentSecurityQuestion = null;
            CompanyName = "New Company Name";
            CompanyPassword = "Password";
            SecurityAnswer = "answer";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void CompanyInvalidWhenNull()
        {
            Name = "Name";
            Password = "password";
            EmailAddress = "fakeemail@gmail.com";
            CurrentSecurityQuestion = new securityquestion();
            CurrentSecurityQuestion.SecurityQuestionId = 1;
            CompanyName = null;
            CompanyPassword = "Password";
            SecurityAnswer = "answer";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void CompanyInvalidWhenWhitespace()
        {
            Name = "Name";
            Password = "password";
            EmailAddress = "fakeemail@gmail.com";
            CurrentSecurityQuestion = new securityquestion();
            CurrentSecurityQuestion.SecurityQuestionId = 1;
            CompanyName = String.Empty;
            CompanyPassword = "Password";
            SecurityAnswer = "answer";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void CompanyInvalidWhenGreaterThanMaxLength()
        {
            Name = "Name";
            Password = "password";
            EmailAddress = "fakeemail@gmail.com";
            CurrentSecurityQuestion = new securityquestion();
            CurrentSecurityQuestion.SecurityQuestionId = 1;
            CompanyName = "kjfhsdkfhdshfjsdjkfhksdjfhksdkfhsdkjfhsdkjfhsdkhfjsdkhfjshdkfhdsk";
            CompanyPassword = "Password";
            SecurityAnswer = "answer";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void CompanyInvalidWhenLessThanMinLength()
        {
            Name = "Name";
            Password = "password";
            EmailAddress = "fakeemail@gmail.com";
            CurrentSecurityQuestion = new securityquestion();
            CurrentSecurityQuestion.SecurityQuestionId = 1;
            CompanyName = "C";
            CompanyPassword = "Password";
            SecurityAnswer = "answer";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void CompanyInvalidWhenInvalidCharacters()
        {
            Name = "Name";
            Password = "password";
            EmailAddress = "fakeemail@gmail.com";
            CurrentSecurityQuestion = new securityquestion();
            CurrentSecurityQuestion.SecurityQuestionId = 1;
            CompanyName = "Company!";
            CompanyPassword = "Password";
            SecurityAnswer = "answer";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void CompanyPasswordInvalidWhenNull()
        {
            Name = "Name";
            Password = "password";
            EmailAddress = "fakeemail@gmail.com";
            CurrentSecurityQuestion = new securityquestion();
            CurrentSecurityQuestion.SecurityQuestionId = 1;
            CompanyName = "CompanyName";
            CompanyPassword = null;
            SecurityAnswer = "answer";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void CompanyPasswordInvalidWhenWhitespace()
        {
            Name = "Name";
            Password = "password";
            EmailAddress = "fakeemail@gmail.com";
            CurrentSecurityQuestion = new securityquestion();
            CurrentSecurityQuestion.SecurityQuestionId = 1;
            CompanyName = "CompanyName";
            CompanyPassword = String.Empty;
            SecurityAnswer = "answer";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void CompanyPasswordInvalidWhenGreaterThanMaxLength()
        {
            Name = "Name";
            Password = "password";
            EmailAddress = "fakeemail@gmail.com";
            CurrentSecurityQuestion = new securityquestion();
            CurrentSecurityQuestion.SecurityQuestionId = 1;
            CompanyName = "CompanyName";
            CompanyPassword = "Passworddfsdfsdfndsmfdbsfbdsbfbdsfbsdfbsdfd";
            SecurityAnswer = "answer";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void CompanyPasswordInvalidWhenLessThanMinLength()
        {
            Name = "Name";
            Password = "password";
            EmailAddress = "fakeemail@gmail.com";
            CurrentSecurityQuestion = new securityquestion();
            CurrentSecurityQuestion.SecurityQuestionId = 1;
            CompanyName = "CompanyName";
            CompanyPassword = "Pas";
            SecurityAnswer = "answer";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void CompanyPasswordInvalidWhenInvalidCharacters()
        {
            Name = "Name";
            Password = "password";
            EmailAddress = "fakeemail@gmail.com";
            CurrentSecurityQuestion = new securityquestion();
            CurrentSecurityQuestion.SecurityQuestionId = 1;
            CompanyName = "CompanyName";
            CompanyPassword = "Password!";
            SecurityAnswer = "answer";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void SecurityAnswerInvalidWhenNull()
        {
            Name = "Name";
            Password = "password";
            EmailAddress = "fakeemail@gmail.com";
            CurrentSecurityQuestion = new securityquestion();
            CurrentSecurityQuestion.SecurityQuestionId = 1;
            CompanyName = "CompanyName";
            CompanyPassword = "Password";
            SecurityAnswer = null;

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void SecurityAnswerInvalidWhenWhitespace()
        {
            Name = "Name";
            Password = "password";
            EmailAddress = "fakeemail@gmail.com";
            CurrentSecurityQuestion = new securityquestion();
            CurrentSecurityQuestion.SecurityQuestionId = 1;
            CompanyName = "CompanyName";
            CompanyPassword = "Password";
            SecurityAnswer = String.Empty;

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void SecurityAnswerInvalidWhenGreaterThanMaxLength()
        {
            Name = "Name";
            Password = "password";
            EmailAddress = "fakeemail@gmail.com";
            CurrentSecurityQuestion = new securityquestion();
            CurrentSecurityQuestion.SecurityQuestionId = 1;
            CompanyName = "CompanyName";
            CompanyPassword = "Password";
            SecurityAnswer = "answerfsdfsdfsdfsdfsdfsdfsdfdsf";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void SecurityAnswerInvalidWhenLessThanMinLength()
        {
            Name = "Name";
            Password = "password";
            EmailAddress = "fakeemail@gmail.com";
            CurrentSecurityQuestion = new securityquestion();
            CurrentSecurityQuestion.SecurityQuestionId = 1;
            CompanyName = "CompanyName";
            CompanyPassword = "Password";
            SecurityAnswer = "a";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        [Test]
        public void SecurityAnswerInvalidWhenInvalidCharacters()
        {
            Name = "Name";
            Password = "password";
            EmailAddress = "fakeemail@gmail.com";
            CurrentSecurityQuestion = new securityquestion();
            CurrentSecurityQuestion.SecurityQuestionId = 1;
            CompanyName = "CompanyName";
            CompanyPassword = "Password";
            SecurityAnswer = "answer!";

            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.RegisterUserCommand.CanExecute(""));
        }

        #endregion

        #region Command Tests

        //Problem with s => s.SaveUser(.newUser)
        [Test]
        public void ShouldRegisterUserIfNonExistingEmailAddress()
        {
            #region Setup
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

            List<companydetail> listOfCompanies = new List<companydetail>();

            companydetail fakeCompany = new companydetail();
            fakeCompany.CompanyName = "Fake company";
            fakeCompany.CompanyPassword = "password";
            fakeCompany.UserId = 1;
            fakeCompany.CompanyId = 1;

            listOfCompanies.Add(fakeCompany);

            CompanyName = "Company Name";
            CompanyPassword = fakeCompany.CompanyPassword;

            long id = 1;
            #endregion 

            serviceClient.Setup(s => s.GetExistingUsersAsync()).Returns(Task.FromResult(listOfUsers.ToArray()));
            serviceClient.Setup(s => s.SaveUserAsync(It.IsAny<user>())).Returns(Task.FromResult(id));
            serviceClient.Setup(s => s.saveCompanyAsync(It.IsAny<companydetail>())).Verifiable();
            serviceClient.Setup(s => s.SaveUserRoleAsync(It.IsAny<userrole>())).Verifiable();
            serviceClient.Setup(s => s.getExistingCompaniesAsync()).Returns(Task.FromResult(listOfCompanies.ToArray()));
            serviceClient.Setup(s => s.SaveUserSecurityQuestionAsync(It.IsAny<usersecurityquestion>())).Verifiable();
            
            viewModel.ExecuteRegisterUserCommand();

            serviceClient.Verify(s => s.GetExistingUsersAsync(), Times.Exactly(1));        
            serviceClient.Verify(s => s.SaveUserRoleAsync(It.IsAny<userrole>()), Times.Exactly(1));
            serviceClient.Verify(s => s.getExistingCompaniesAsync(), Times.Exactly(1));
            serviceClient.Verify(s => s.saveCompanyAsync(It.IsAny<companydetail>()), Times.Exactly(1));
            serviceClient.Verify(s => s.SaveUserSecurityQuestionAsync(It.IsAny<usersecurityquestion>()), Times.Exactly(1));
            serviceClient.Verify(s => s.SaveUserAsync(It.IsAny<user>()), Times.Exactly(1));
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

            List<companydetail> listOfCompanies = new List<companydetail>();

            companydetail fakeCompany = new companydetail();
            fakeCompany.CompanyName = "Fake company";
            fakeCompany.CompanyPassword = "password";
            fakeCompany.UserId = 1;
            fakeCompany.CompanyId = 1;

            listOfCompanies.Add(fakeCompany);

            CompanyName = "Company Name";
            CompanyPassword = fakeCompany.CompanyPassword;

            long id = 1;

            List<user> listOfUsers = new List<user>();
            listOfUsers.Add(existingUser);

            serviceClient.Setup(s => s.GetExistingUsersAsync()).Returns(Task.FromResult(listOfUsers.ToArray()));
            serviceClient.Setup(s => s.SaveUserAsync(It.IsAny<user>())).Returns(Task.FromResult(id));
            serviceClient.Setup(s => s.saveCompanyAsync(It.IsAny<companydetail>())).Verifiable();
            serviceClient.Setup(s => s.SaveUserRoleAsync(It.IsAny<userrole>())).Verifiable();
            serviceClient.Setup(s => s.getExistingCompaniesAsync()).Returns(Task.FromResult(listOfCompanies.ToArray()));
            serviceClient.Setup(s => s.SaveUserSecurityQuestionAsync(It.IsAny<usersecurityquestion>())).Verifiable();

            viewModel.ExecuteRegisterUserCommand();

            serviceClient.Verify(s => s.GetExistingUsersAsync(), Times.Exactly(1));
            serviceClient.Verify(s => s.SaveUserRoleAsync(It.IsAny<userrole>()), Times.Exactly(0));
            serviceClient.Verify(s => s.getExistingCompaniesAsync(), Times.Exactly(1));
            serviceClient.Verify(s => s.saveCompanyAsync(It.IsAny<companydetail>()), Times.Exactly(0));
            serviceClient.Verify(s => s.SaveUserSecurityQuestionAsync(It.IsAny<usersecurityquestion>()), Times.Exactly(0));
            serviceClient.Verify(s => s.SaveUserAsync(It.IsAny<user>()), Times.Exactly(0));
        }

        [Test]
        public void ShouldNotRegisterUserIfCompanyAlreadyExists()
        {
            #region Setup
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

            List<companydetail> listOfCompanies = new List<companydetail>();

            companydetail fakeCompany = new companydetail();
            fakeCompany.CompanyName = "Fake company";
            fakeCompany.CompanyPassword = "password";
            fakeCompany.UserId = 1;

            listOfCompanies.Add(fakeCompany);

            CompanyName = fakeCompany.CompanyName;
            CompanyPassword = fakeCompany.CompanyPassword;

            long id = 1;
            #endregion

            serviceClient.Setup(s => s.GetExistingUsersAsync()).Returns(Task.FromResult(listOfUsers.ToArray()));
            serviceClient.Setup(s => s.SaveUserAsync(It.IsAny<user>())).Returns(Task.FromResult(id));
            serviceClient.Setup(s => s.saveCompanyAsync(fakeCompany)).Verifiable();
            serviceClient.Setup(s => s.SaveUserRoleAsync(It.IsAny<userrole>())).Verifiable();
            serviceClient.Setup(s => s.getExistingCompaniesAsync()).Returns(Task.FromResult(listOfCompanies.ToArray()));
            serviceClient.Setup(s => s.SaveUserSecurityQuestionAsync(It.IsAny<usersecurityquestion>())).Verifiable();

            viewModel.ExecuteRegisterUserCommand();

            serviceClient.Verify(s => s.GetExistingUsersAsync(), Times.Exactly(1));
            serviceClient.Verify(s => s.SaveUserRoleAsync(It.IsAny<userrole>()), Times.Exactly(0));
            serviceClient.Verify(s => s.getExistingCompaniesAsync(), Times.Exactly(1));
            serviceClient.Verify(s => s.saveCompanyAsync(It.IsAny<companydetail>()), Times.Exactly(0));
            serviceClient.Verify(s => s.SaveUserSecurityQuestionAsync(It.IsAny<usersecurityquestion>()), Times.Exactly(0));
            serviceClient.Verify(s => s.SaveUserAsync(It.IsAny<user>()), Times.Exactly(0));
        }
        #endregion
    }
}
