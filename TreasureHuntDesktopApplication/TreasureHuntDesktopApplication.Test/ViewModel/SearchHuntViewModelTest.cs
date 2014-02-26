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
    class SearchHuntViewModelTest
    {
        #region Setup

        public SearchHuntViewModel viewModel;
        Mock<ITreasureHuntService> serviceClient;
        hunt myFakeHunt1;
        hunt myFakeHunt2;
        List<hunt> returnedHunts = new List<hunt>();

        [SetUp]
        public void Setup()
        {
            serviceClient = new Mock<ITreasureHuntService>();
            viewModel = new SearchHuntViewModel(serviceClient.Object);
            
            myFakeHunt1 = new hunt();
            myFakeHunt1.HuntName = "My Fake Hunt 1";

            myFakeHunt2 = new hunt();
            myFakeHunt2.HuntName = "My Fake Hunt 2";

            returnedHunts.Add(myFakeHunt1);
            returnedHunts.Add(myFakeHunt2);
        }
        #endregion

        #region Test Variables

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

        public IEnumerable<hunt> TreasureHunts
        {
            get
            {
                return viewModel.TreasureHunts;
            }
            set
            {
                viewModel.TreasureHunts = value;
            }     
        }



        #endregion

        #region Validation Tests
        [Test]
        public void ShouldReturnInvalidIfCurrentTreasureHuntIsNull()
        {
            this.CurrentTreasureHunt = null;
            Assert.False(viewModel.IsValidHunt());
            Assert.False(viewModel.SearchHuntCommand.CanExecute(""));
        }
        #endregion

        #region RefreshingDataTests/ServiceCalls

        [Test]
        public void ShouldReturnAllTreasureHuntsFromDatabaseForAParticularUser()
        {
            user fakeUser = new user();
            fakeUser.Name="Current users name";
            fakeUser.UserId = 1;

            CurrentUser = fakeUser;

            serviceClient.Setup<hunt[]>(s => s.GetTreasureHuntsForParticularUser(fakeUser)).Returns(returnedHunts.ToArray());

            viewModel.RefreshTreasureHunts();

            serviceClient.Verify(s => s.GetTreasureHuntsForParticularUser(fakeUser), Times.Exactly(1));
            Assert.AreEqual(returnedHunts, TreasureHunts);
        }
        #endregion
    }
}
