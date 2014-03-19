using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight.Messaging;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using TreasureHuntDesktopApplication.FullClient.Messages;
using TreasureHuntDesktopApplication.FullClient.Project_Utilities;
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
        public RelayCommand ResetCompanyPasswordCommand { get; private set; }

        public SearchHuntViewModel(ITreasureHuntService _serviceClient)
        {
            serviceClient = _serviceClient;
            SearchHuntCommand = new RelayCommand(() => ExecuteSearchHuntCommand(), () => IsValidHunt());
            CreateNewHuntCommand = new RelayCommand(() => ExecuteCreateHuntCommand());
            LogoutCommand = new RelayCommand(() => ExecuteLogoutCommand());
            ResetCompanyPasswordCommand = new RelayCommand(() => ExecuteResetCompanyPasswordCommand());
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

           PopupDisplayed = false;
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

        #region Refreshing Data

        //make internal
        public async void RefreshTreasureHunts()
        {
            PopupDisplayed = true;
            TreasureHunts = await this.serviceClient.GetTreasureHuntsForParticularUserAsync(this.currentUser);
            CurrentTreasureHunt = null;
            PopupDisplayed = false;
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
            //(-http://etaktix.blogspot.co.uk/2013/01/check-if-internet-connection-is.html)
            if (InternetConnectionChecker.IsInternetConnected())
            {
                //Takes the user to the selected hunt page.
                Messenger.Default.Send<SelectedHuntMessage>(new SelectedHuntMessage() { CurrentHunt = this.currentTreasureHunt });
                Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "ViewHuntViewModel" });
            }
            else
            {
                MessageBoxResult messageBox = MessageBox.Show(InternetConnectionChecker.ShowConnectionErrorMessage());
            }
        }

        private void ExecuteCreateHuntCommand()
        {
            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "CreateHuntViewModel" });
        }

        private void ExecuteLogoutCommand()
        {
            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "LoginViewModel" });
            //SHOULD RESET THE CURRENT USER TO EMPTY
        }

        private void ExecuteResetCompanyPasswordCommand()
        {
            Messenger.Default.Send<CurrentUserMessage>(new CurrentUserMessage() { CurrentUser = this.CurrentUser });
            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "ResetCompanyPasswordViewModel" });    
        }

        #endregion
    }
}
