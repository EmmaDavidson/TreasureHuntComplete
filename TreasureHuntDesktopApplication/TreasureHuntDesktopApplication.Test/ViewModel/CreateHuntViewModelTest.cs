using System;
using NUnit.Framework;
using TreasureHuntDesktopApplication.FullClient.ViewModel;
using Moq;
using TreasureHuntDesktopApplication.FullClient.TreasureHuntService;
using System.Collections.Generic;
using System.Threading.Tasks;

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
        #endregion

        #region Validation Tests

        [Test]
        public void HuntNameInvalidWhenNull()
        {
            String nullHuntName = null;
            HuntName = nullHuntName;
            Description = "description";
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.SaveHuntCommand.CanExecute(""));
        }

        [Test]
        public void HuntNameInvalidWhenWhitespace()
        {
            String WhitespaceHuntName = String.Empty;
            Description = "description";
            HuntName = WhitespaceHuntName;
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.SaveHuntCommand.CanExecute(""));
        }

        [Test]
        public void HuntNameInvalidWhenGreaterThanMaxLength() 
        {
            String LongHuntName = "abcdefghijklmnopqrstidshvfsdhkfhdskjfjdhfkhdsjhfjhdshfdsjfdshfhdjhfkjurtiuerutureiutoeriutoireuiotueroituoreutoierutioeuroituerituieoroutireuotieruoitueroitieruotuertsdhfhdu";
            HuntName = LongHuntName;
            Description = "description";
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.SaveHuntCommand.CanExecute(""));
        }

        [Test]
        public void HuntNameInvalidWhenLessThanMinLength()
        {
            String LongHuntName = "a";
            HuntName = LongHuntName;
            Description = "description";
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.SaveHuntCommand.CanExecute(""));
        }

        [Test]
        public void DescriptionInvalidWhenNull()
        {
            String description = null;
            Description = description;
            HuntName = "huntName";
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.SaveHuntCommand.CanExecute(""));
        }

        [Test]
        public void DescriptionInvalidWhenWhitespace()
        {
            String WhitespaceDescription = String.Empty;
            Description = WhitespaceDescription;
            HuntName = "huntName";
            //RetypedPassword = "password";
            //Password = "password";
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.SaveHuntCommand.CanExecute(""));
        }

        [Test]
        public void DescriptionInvalidWhenLessThanMinLength()
        {
            String WhitespaceDescription = "Des";
            Description = WhitespaceDescription;
            HuntName = "huntName";
            Assert.False(viewModel.IsValidDetails());
            Assert.False(viewModel.SaveHuntCommand.CanExecute(""));
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
            this.Description = "Fake description";

            long resultId = 1; 

            #endregion

            //-http://stackoverflow.com/questions/20859639/using-moq-to-mock-an-asynchronous-method-for-a-unit-test
            serviceClient.Setup(s => s.GetTreasureHuntsForParticularUserAsync(currentUser)).Returns(Task.FromResult(listOfHunts.ToArray()));
            serviceClient.Setup(s => s.SaveNewHuntAsync(It.IsAny<hunt>())).Returns(Task.FromResult(resultId));
            serviceClient.Setup(s => s.SaveUserHuntAsync(It.IsAny<userhunt>())).Returns(Task.FromResult(""));
            serviceClient.Setup(s => s.GetHuntBasedOnNameAsync(It.IsAny<string>(), It.IsAny<long>())).Returns(Task.FromResult(fakeHunt));
   
            viewModel.ExecuteSaveCommand();

            serviceClient.Verify(s => s.SaveNewHuntAsync(It.IsAny<hunt>()), Times.Exactly(1));
            serviceClient.Verify(s => s.GetTreasureHuntsForParticularUserAsync(currentUser), Times.Exactly(1));
            serviceClient.Verify(s => s.SaveUserHuntAsync(It.IsAny<userhunt>()), Times.Exactly(1));
            serviceClient.Verify(s => s.GetHuntBasedOnNameAsync(It.IsAny<String>(), It.IsAny<long>()), Times.Exactly(1));
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
            //this.Password = "description";
            this.Description = "Fake description";

            long resultId = 1;
            #endregion

            serviceClient.Setup(s => s.GetTreasureHuntsForParticularUserAsync(currentUser)).Returns(Task.FromResult(listOfHunts.ToArray()));
            serviceClient.Setup(s => s.SaveNewHuntAsync(It.IsAny<hunt>())).Returns(Task.FromResult(resultId));
            serviceClient.Setup(s => s.SaveUserHuntAsync(It.IsAny<userhunt>())).Returns(Task.FromResult(""));
            serviceClient.Setup(s => s.GetHuntBasedOnNameAsync(It.IsAny<string>(), It.IsAny<long>())).Returns(Task.FromResult(fakeHunt));
   
            viewModel.ExecuteSaveCommand();

            serviceClient.Verify(s => s.SaveNewHuntAsync(It.IsAny<hunt>()), Times.Exactly(0));
            serviceClient.Verify(s => s.GetTreasureHuntsForParticularUserAsync(currentUser), Times.Exactly(1));
            serviceClient.Verify(s => s.SaveUserHuntAsync(It.IsAny<userhunt>()), Times.Exactly(0));
            serviceClient.Verify(s => s.GetHuntBasedOnNameAsync(It.IsAny<String>(), It.IsAny<long>()), Times.Exactly(0));
        }
        #endregion
    }
}