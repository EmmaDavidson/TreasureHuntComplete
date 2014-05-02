using Moq;
using NUnit.Framework;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using TreasureHuntDesktopApplication.FullClient.TreasureHuntService;
using TreasureHuntDesktopApplication.FullClient.Utilities;
using TreasureHuntDesktopApplication.FullClient.ViewModel;

namespace TreasureHuntDesktopApplication.Test.ViewModel
{
    //----------------------------------------------------------
    //<copyright>
    //Emma Davidson - Treasure Hunt 2013-3014 Final Year Project
    //</copyright>
    //----------------------------------------------------------
    [TestFixture]
    class LeaderboardViewModelTest
    {
        #region Setup
        public LeaderboardViewModel viewModel;
        Mock<ITreasureHuntService> serviceClient;

        hunt myFakeHunt;

        [SetUp]
        public void Setup()
        {
            serviceClient = new Mock<ITreasureHuntService>();
            viewModel = new LeaderboardViewModel(serviceClient.Object);

            myFakeHunt = new hunt();
            myFakeHunt.HuntName = "My Fake Hunt";

            CurrentTreasureHunt = myFakeHunt;
        }

        #region Variables
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

        public ObservableCollection<Participant> LeaderboardResults
        {
            get
            {
                return viewModel.LeaderboardResults;
            }
            set
            {
                viewModel.LeaderboardResults = value;
            }
        }
        #endregion
        #endregion

        #region Commands tests
        [Test]
        public void ShouldReturnListOfResultsForParticularHunt()
        {
            List<huntparticipant> huntParticipants = new List<huntparticipant>();

            user newUser = new user();
            newUser.UserId = 1;
            newUser.Name = "Fake User";

            huntparticipant participant = new huntparticipant();
            participant.HuntId = 1;
            participant.UserId = 1;
            participant.HuntParticipantId = 1;
            participant.ElapsedTime = 1;
            participant.Tally = 1;

            huntParticipants.Add(participant);

            serviceClient.Setup(s => s.GetHuntParticipantsAsync(myFakeHunt)).Returns(Task.FromResult(huntParticipants.ToArray()));
            serviceClient.Setup(s => s.GetParticipantAsync(newUser.UserId)).Returns(Task.FromResult(newUser));

            viewModel.RefreshLeaderboard();

            serviceClient.Verify(s => s.GetHuntParticipantsAsync(myFakeHunt), Times.Exactly(1));
            serviceClient.Verify(s => s.GetParticipantAsync(newUser.UserId), Times.Exactly(1));

            Participant newParticipant = new Participant(newUser.Name, participant.Tally, participant.ElapsedTime);
            ObservableCollection<Participant> participantsList = new ObservableCollection<Participant>();
            participantsList.Add(newParticipant);

            //Values differ apparently but look the same to me. Needs checked again.
            Assert.AreEqual(participantsList, LeaderboardResults);
        #endregion
        }
    }
}
