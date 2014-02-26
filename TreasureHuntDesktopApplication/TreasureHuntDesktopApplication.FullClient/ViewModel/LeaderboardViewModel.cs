using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight.Messaging;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Threading;
using TreasureHuntDesktopApplication.FullClient.Messages;
using TreasureHuntDesktopApplication.FullClient.TreasureHuntService;
using TreasureHuntDesktopApplication.FullClient.Utilities;

namespace TreasureHuntDesktopApplication.FullClient.ViewModel
{
    public class LeaderboardViewModel : ViewModelBase
    {
        #region Setup
        ITreasureHuntService serviceClient;
        public RelayCommand BackCommand { get; private set; }
        public RelayCommand RefreshCommand { get; private set; }
        public RelayCommand LogoutCommand { get; private set; }
        DispatcherTimer refreshTimer = new DispatcherTimer();

        public LeaderboardViewModel(ITreasureHuntService _serviceClient)
        {
            serviceClient = _serviceClient;
            BackCommand = new RelayCommand(() => ExecuteBackCommand());
            RefreshCommand = new RelayCommand(() => RefreshLeaderboard());
            LogoutCommand = new RelayCommand(() => ExecuteLogoutCommand());

            Messenger.Default.Register<LeaderboardMessage>
             (

                 this,
                 (action) => ReceiveLeaderboardMessage(action.CurrentHunt)

             );

            RefreshLeaderboard();    
        }

        #region Received Message Methods
        private void ReceiveLeaderboardMessage(hunt currentHunt)
        {
            this.currentTreasureHunt = currentHunt;
            RefreshLeaderboard();
        }
        #endregion
        #endregion

        #region Variables
        private ObservableCollection<Participant> leaderboardResults;
        public ObservableCollection<Participant> LeaderboardResults
        {
            get 
            {
                return this.leaderboardResults; 
            }
            set
            {
                this.leaderboardResults = value;
                RaisePropertyChanged("LeaderboardResults");
            }
        }

        private hunt currentTreasureHunt;
        public hunt CurrentTreasureHunt
        {
            get { return this.currentTreasureHunt; }
            set
            {
                this.currentTreasureHunt = value;
                RaisePropertyChanged("CurrentTreasureHunt");

            }
        }
        #endregion

        #region Commands and methods
        private void leaderboardUpdater(object sender, EventArgs e)
        {
            RefreshLeaderboard();
        }

        private void ExecuteBackCommand()
        {
            //refreshTimer.Stop();
            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "ViewHuntViewModel" });
            Messenger.Default.Send<SelectedHuntMessage>(new SelectedHuntMessage() { CurrentHunt = this.currentTreasureHunt });
        }

        private void ExecuteLogoutCommand()
        {
            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "LoginViewModel" });
        }

        public void RefreshLeaderboard()
        {
            if (this.currentTreasureHunt != null)
            {
                var results = new ObservableCollection<Participant>();

                List<huntparticipant> huntParticipants = this.serviceClient.GetHuntParticipants(CurrentTreasureHunt).ToList();

                using (var participants = huntParticipants.GetEnumerator())
                {
                    while (participants.MoveNext())
                    {
                        user currentUser = this.serviceClient.GetParticipantName(participants.Current.UserId);
                        Participant newParticipant = new Participant(currentUser.Name, participants.Current.Tally, participants.Current.ElapsedTime );
                        results.Add(newParticipant);
                    }
                    //Learnt this from previous similar scenario in agile module
                    LeaderboardResults = results;
                }
            }
        }
        #endregion
    }
}

