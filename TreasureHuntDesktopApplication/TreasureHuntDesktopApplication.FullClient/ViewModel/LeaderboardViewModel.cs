using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight.Messaging;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Threading;
using TreasureHuntDesktopApplication.FullClient.Messages;
using TreasureHuntDesktopApplication.FullClient.Project_Utilities;
using TreasureHuntDesktopApplication.FullClient.TreasureHuntService;
using TreasureHuntDesktopApplication.FullClient.Utilities;

//----------------------------------------------------------
//<copyright>
/*
 * Emma Davidson - Treasure Hunt 2013-3014 Final Year Project
 */
//</copyright>
//----------------------------------------------------------

namespace TreasureHuntDesktopApplication.FullClient.ViewModel
{
    /// <Summary> This is the ViewModel associated with the LeaderboardView and is responsible for the interaction
    /// between the View and the Model for displaying a leader board for a particular treasure hunt.
    /// See Dissertation Section 2.4.1.7 </Summary>

    public class LeaderboardViewModel : ViewModelBase
    {
        #region Setup

        #region Fields
        #region General global variables
       
        private ITreasureHuntService serviceClient;
        public RelayCommand BackCommand { get; private set; }
        public RelayCommand RefreshCommand { get; private set; }

        private InternetConnectionChecker connectionChecker;

        #endregion

        #region Binding variables
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

        private bool popupDisplayed;
        public bool PopupDisplayed
        {
            get { return this.popupDisplayed; }
            set
            {
                this.popupDisplayed = value;
                RaisePropertyChanged("PopupDisplayed");
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
 
        #endregion 

        #region Constructor
        public LeaderboardViewModel(ITreasureHuntService serviceClient)
        {
            this.serviceClient = serviceClient;
            BackCommand = new RelayCommand(() => ExecuteBackCommand());
            RefreshCommand = new RelayCommand(() => RefreshLeaderboard());

            PopupDisplayed = false;

            Messenger.Default.Register<LeaderboardMessage>
             (

                 this,
                 (action) => ReceiveLeaderboardMessage(action.CurrentHunt)

             );

            connectionChecker = InternetConnectionChecker.GetInstance();

            RefreshLeaderboard();
        }
        #endregion

        #region Received Messages
        /// <summary> Method used to receive an incoming LeaderboardMessage to store the data related to the current  
        /// hunt being accessed. </summary>
        private void ReceiveLeaderboardMessage(hunt currentHunt)
        {
            this.currentTreasureHunt = currentHunt;
            RefreshLeaderboard();
        }
        #endregion
        #endregion

        #region Methods

        /// <summary>
        /// Method that will navigate the administrator back to the ViewHuntView.
        /// </summary>
        private void ExecuteBackCommand()
        {
            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "ViewHuntViewModel" });
            Messenger.Default.Send<SelectedHuntMessage>(new SelectedHuntMessage() { CurrentHunt = this.currentTreasureHunt });
        }

        /// <summary>
        /// Method that will refresh the results of the leader board with the latest information from the database.
        /// </summary>
        public async void RefreshLeaderboard()
        {
            PopupDisplayed = true;
            
            if (connectionChecker.IsInternetConnected())
            {
                if (this.currentTreasureHunt != null)
                {
                    var results = new ObservableCollection<Participant>();

                    //Grab a list of all of the participants in this treasure hunt.
                    IEnumerable<huntparticipant> huntParticipants = this.serviceClient.GetHuntParticipantsAsync(CurrentTreasureHunt).Result.ToList();

                    if (huntParticipants != null)
                    {
                        //For each participant represented by an id in this list
                        using (var participants = huntParticipants.GetEnumerator())
                        {
                            while (participants.MoveNext())
                            {
                                //Get that participants details and add them to the list to be displayed
                                user currentUser = await this.serviceClient.GetParticipantAsync(participants.Current.UserId);
                                Participant newParticipant = new Participant(currentUser.Name, participants.Current.Tally, participants.Current.ElapsedTime);
                                results.Add(newParticipant);
                            }
                            results.OrderBy(i => i.ElapsedTime);
                            LeaderboardResults = results;
                        }
                    }
                }
            }
            else
            {
                MessageBoxResult messageBox = MessageBox.Show(connectionChecker.ShowConnectionErrorMessage());
            }
           
            PopupDisplayed = false;          
        }
        #endregion
    }
}

