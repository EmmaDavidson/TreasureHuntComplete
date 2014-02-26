using System;
using NUnit.Framework;
using TreasureHuntDesktopApplication.FullClient.ViewModel;
using Moq;
using TreasureHuntDesktopApplication.FullClient.TreasureHuntService;
using System.Collections.Generic;

namespace TreasureHuntDesktopApplication.Test
{
    [TestFixture]
    public class CreateHuntViewModelTest
    {
        #region Setup

        public CreateHuntViewModel viewModel;
        Mock<ITreasureHuntService> serviceClient;

        public const string huntName = "My New Treasure Hunt";

        [SetUp]
        public void Setup()
        {   serviceClient = new Mock<ITreasureHuntService>();
            viewModel = new CreateHuntViewModel(serviceClient.Object);
            viewModel.HuntName = huntName;
        }

        public user CurrentUser
        {
            get
            { 
                return viewModel.CurrentUser; 
            }
            set
            {
                viewModel.CurrentUser = value;  
            }
        }

        public String HuntName
        {
            get
            {
                return viewModel.HuntName;
            }
            set
            {
                viewModel.HuntName = value;
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

        public String Description
        {
            get 
            { 
                return viewModel.Description; 
            }
            set
            {
                viewModel.Description = value;
            }
        }

        private String password;
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
        #endregion

        #region Validation Tests

        [Test]
        public void HuntNameInvalidWhenNull()
        {
            String nullHuntName = null;
            HuntName = nullHuntName;
            Password = "password";
            RetypedPassword = "password";
            Description = "description";
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.SaveHuntNameCommand.CanExecute(""));
        }

        [Test]
        public void HuntNameInvalidWhenWhitespace()
        {
            String WhitespaceHuntName = String.Empty;
            Password = "password";
            RetypedPassword = "password";
            Description = "description";
            HuntName = WhitespaceHuntName;
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.SaveHuntNameCommand.CanExecute(""));
        }

        [Test]
        public void HuntNameInvalidWhenGreaterThanMaxLength() 
        {
            String LongHuntName = "abcdefghijklmnopqrstidshvfsdhkfhdskjfjdhfkhdsjhfjhdshfdsjfdshfhdjhfkjsdhfhdu";
            HuntName = LongHuntName;
            Password = "password";
            RetypedPassword = "password";
            Description = "description";
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.SaveHuntNameCommand.CanExecute(""));
        }

        [Test]
        public void HuntNameInvalidWhenLessThanMinLength()
        {
            String LongHuntName = "a";
            HuntName = LongHuntName;
            Password = "password";
            RetypedPassword = "password";
            Description = "description";
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.SaveHuntNameCommand.CanExecute(""));
        }

        [Test]
        public void PasswordInvalidWhenNull()
        {
            String password = null;
            Password = password;
            HuntName = "huntName";
            RetypedPassword = "password";
            Description = "description";
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.SaveHuntNameCommand.CanExecute(""));
        }

        [Test]
        public void PasswordInvalidWhenWhitespace()
        {
            String WhitespacePassword = String.Empty;
            Password = WhitespacePassword;
            HuntName = "huntName";
            RetypedPassword = "password";
            Description = "description";
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.SaveHuntNameCommand.CanExecute(""));
        }

        [Test]
        public void PasswordInvalidWhenGreaterThanMaxLength()
        {
            String LongPassword = "abcdefghijklmnopqrstidshvfsdhkfhdskjfjdhfkhdsjhfjhdshfdsjfdshfhdjhfkjsdhfhdu";
            Password = LongPassword;
            HuntName = "huntName";
            RetypedPassword = "password";
            Description = "description";
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.SaveHuntNameCommand.CanExecute(""));
        }

        [Test]
        public void PasswordInvalidWhenLessThanMinLength()
        {
            String LongPassword = "a";
            Password = LongPassword;
            HuntName = "huntName";
            RetypedPassword = "password";
            Description = "description";
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.SaveHuntNameCommand.CanExecute(""));
        }

        [Test]
        public void DescriptionInvalidWhenNull()
        {
            String description = null;
            Description = description;
            HuntName = "huntName";
            RetypedPassword = "password";
            Password = "password";
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.SaveHuntNameCommand.CanExecute(""));
        }

        [Test]
        public void DescriptionInvalidWhenWhitespace()
        {
            String WhitespaceDescription = String.Empty;
            Description = WhitespaceDescription;
            HuntName = "huntName";
            RetypedPassword = "password";
            Password = "password";
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.SaveHuntNameCommand.CanExecute(""));
        }

        //Description is too long to test for max length
        [Test]
        public void DescriptionInvalidWhenLessThanMinLength()
        {
            String LongPassword = "a";
            Password = LongPassword;
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.SaveHuntNameCommand.CanExecute(""));
        }

        #endregion

        #region Service Call Tests

        [Test]
        public void ShouldSaveNewTreasureHuntIfDoesNotAlreadyExist()
        {
            #region Setup variables
            userrole userRole = new userrole();
            userRole.UserId = 1;
            userRole.RoleId = 1;
            userRole.UserRoleId = 1;

            hunt fakeHunt = new hunt();
            fakeHunt.HuntId = 1;
            fakeHunt.HuntName= "this fake hunt";

            hunt fakeHuntTwo = new hunt();
            fakeHuntTwo.HuntId = 2;
            fakeHuntTwo.HuntName = "my second fake hunt";

            user currentUser = new user();
            currentUser.Name = "fake user";
            currentUser.UserId = 1;

            List<hunt> listOfHunts = new List<hunt>();
            listOfHunts.Add(fakeHuntTwo);
            
            this.HuntName = "This fake hunt";
            this.CurrentUser = currentUser;
            this.Password = "description";
            this.Description = "Fake description";
            #endregion

            serviceClient.Setup(s => s.GetTreasureHunts()).Returns(listOfHunts.ToArray());
            serviceClient.Setup(s => s.SaveNewHunt(It.IsAny<hunt>())).Returns(1);
            serviceClient.Setup(s => s.GetUserRole(It.IsAny<user>())).Returns(userRole);
            serviceClient.Setup(s => s.SaveUserHunt(It.IsAny<userhunt>())).Verifiable();
            serviceClient.Setup(s => s.GetHuntBasedOnName(fakeHunt.HuntName)).Returns(fakeHunt);
   
            viewModel.ExecuteSaveHuntNameCommand();

            serviceClient.Verify(s => s.SaveNewHunt(It.IsAny<hunt>()), Times.Exactly(1));
            serviceClient.Verify(s => s.GetTreasureHunts(), Times.Exactly(1));
            serviceClient.Verify(s => s.GetUserRole(It.IsAny<user>()), Times.Exactly(1));
            serviceClient.Verify(s => s.SaveUserHunt(It.IsAny<userhunt>()), Times.Exactly(1));
            serviceClient.Verify(s => s.GetHuntBasedOnName(It.IsAny<String>()), Times.Exactly(1));
        }

        //Need to silence the pop up box
        [Test]
        public void ShouldNotSaveNewTreasureHuntIfItAlreadyExists()
        {
            #region Setup variables
            userrole userRole = new userrole();
            userRole.UserId = 1;
            userRole.RoleId = 1;
            userRole.UserRoleId = 1;

            hunt fakeHunt = new hunt();
            fakeHunt.HuntId = 1;
            fakeHunt.HuntName = "This fake hunt";

            user currentUser = new user();
            currentUser.Name = "fake user";
            currentUser.UserId = 1;

            List<hunt> listOfHunts = new List<hunt>();
            listOfHunts.Add(fakeHunt);

            this.HuntName = fakeHunt.HuntName;
            this.CurrentUser = currentUser;
            this.Password = "description";
            this.Description = "Fake description";
            #endregion

            serviceClient.Setup(s => s.GetTreasureHunts()).Returns(listOfHunts.ToArray());
            serviceClient.Setup(s => s.SaveNewHunt(It.IsAny<hunt>())).Returns(1);
            serviceClient.Setup(s => s.GetUserRole(It.IsAny<user>())).Returns(userRole);
            serviceClient.Setup(s => s.SaveUserHunt(It.IsAny<userhunt>())).Verifiable();
            serviceClient.Setup(s => s.GetHuntBasedOnName(fakeHunt.HuntName)).Returns(fakeHunt);

            viewModel.ExecuteSaveHuntNameCommand();
        
            serviceClient.Verify(s => s.GetTreasureHunts(), Times.Exactly(1));
            serviceClient.Verify(s => s.SaveNewHunt(It.IsAny<hunt>()), Times.Exactly(0));
            serviceClient.Verify(s => s.GetUserRole(It.IsAny<user>()), Times.Exactly(0));
            serviceClient.Verify(s => s.SaveUserHunt(It.IsAny<userhunt>()), Times.Exactly(0));
            serviceClient.Verify(s => s.GetHuntBasedOnName(It.IsAny<String>()), Times.Exactly(0));
        }
        #endregion
    }
}