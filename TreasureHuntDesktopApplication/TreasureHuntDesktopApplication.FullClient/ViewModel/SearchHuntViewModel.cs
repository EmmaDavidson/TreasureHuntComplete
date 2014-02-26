using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight.Messaging;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using TreasureHuntDesktopApplication.FullClient.Messages;
using TreasureHuntDesktopApplication.FullClient.TreasureHuntService;

namespace TreasureHuntDesktopApplication.FullClient.ViewModel
{
    public class SearchHuntViewModel : ViewModelBase
    {
        #region Setup
        ITreasureHuntService serviceClient;
        public RelayCommand SearchHuntCommand { get; private set; }
        public RelayCommand CreateNewHuntCommand { get; private set; }
        public RelayCommand LogoutCommand { get; private set; }

        public SearchHuntViewModel(ITreasureHuntService _serviceClient)
        {
            serviceClient = _serviceClient;
            SearchHuntCommand = new RelayCommand(() => ExecuteSearchHuntCommand(), () => IsValidHunt());
            CreateNewHuntCommand = new RelayCommand(() => ExecuteCreateHuntCommand());
            LogoutCommand = new RelayCommand(() => ExecuteLogoutCommand());
            RefreshTreasureHunts();

            Messenger.Default.Register<ViewUpdatedMessage>
             (

             this,
             (action) => ReceiveViewUpdatedMessage(action.UpdatedView)

             );

            Messenger.Default.Register<CurrentUserMessage>
           (

           this,
           (action) => ReceiveCurrentUserMessage(action.CurrentUser)

           );
        }

        #endregion

        #region Received Message Methods
        private void ReceiveViewUpdatedMessage(bool updatedView)
        {
            if (updatedView)
            {
                RefreshTreasureHunts();
            }

            //CurrentTreasureHunt = null;  
        }

        private void ReceiveCurrentUserMessage(user currentUser)
        {
            CurrentUser = currentUser;
        }
        #endregion

        #region Variables

        private user currentUser;
        public user CurrentUser
        {
            get { return this.currentUser; }
            set
            {

                this.currentUser = value;
                RaisePropertyChanged("CurrentUser");
            }
        }

        private IEnumerable<hunt> treasureHunts;
        public IEnumerable<hunt> TreasureHunts
        {
            get { return this.treasureHunts; }
            set
            {
                this.treasureHunts = value;
                RaisePropertyChanged("TreasureHunts");
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

        #region Refreshing Data

        //make internal
        public void RefreshTreasureHunts()
        {
            TreasureHunts = this.serviceClient.GetTreasureHuntsForParticularUser(this.currentUser);
            CurrentTreasureHunt = null;
        }
        #endregion

        #region Validation

        //Make internal
        public bool IsValidHunt()
        {
            if (currentTreasureHunt != null)
            {
                return true;
            }
            return false;
        
        }
        #endregion

        #region Commands
        private void ExecuteSearchHuntCommand()
        {
            //Takes the user to the selected hunt page.
            Messenger.Default.Send<SelectedHuntMessage>(new SelectedHuntMessage() { CurrentHunt = this.currentTreasureHunt });
            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "ViewHuntViewModel" });
        }

        private void ExecuteCreateHuntCommand()
        {
            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "CreateHuntViewModel" });
        }

        private void ExecuteLogoutCommand()
        {
            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "LoginViewModel" });
        }

        #endregion
    }
}
