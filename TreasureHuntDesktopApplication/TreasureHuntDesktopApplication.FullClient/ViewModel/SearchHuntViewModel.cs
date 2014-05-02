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

//----------------------------------------------------------
//<copyright>
/*
 * Emma Davidson - Treasure Hunt 2013-3014 Final Year Project
 */
//</copyright>
//----------------------------------------------------------

namespace TreasureHuntDesktopApplication.FullClient.ViewModel
{
    /// <Summary> This is the ViewModel associated with the SearchHuntViewModel and is responsible for the interaction
    /// between the View and the Model to display a list of treasure hunts associated with a particular administrator.
    /// See Dissertation Section 2.4.1.5 </Summary>
    
    public class SearchHuntViewModel : ViewModelBase
    {
        #region Setup

        #region Fields

        #region General global variables
        private ITreasureHuntService serviceClient;
        public RelayCommand SearchHuntCommand { get; private set; }
        public RelayCommand CreateNewHuntCommand { get; private set; }
        public RelayCommand LogoutCommand { get; private set; }
        public RelayCommand ResetCompanyPasswordCommand { get; private set; }

        private InternetConnectionChecker connectionChecker;
        #endregion 

        #region Binding variables

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

        #endregion

        #region Constructor
        public SearchHuntViewModel(ITreasureHuntService serviceClient)
        {
            this.serviceClient = serviceClient;
            SearchHuntCommand = new RelayCommand(() => ExecuteSearchHuntCommand(), () => IsValidHunt());
            CreateNewHuntCommand = new RelayCommand(() => ExecuteCreateHuntCommand());
            LogoutCommand = new RelayCommand(() => ExecuteLogoutCommand());
            ResetCompanyPasswordCommand = new RelayCommand(() => ExecuteResetCompanyPasswordCommand());
            RefreshTreasureHunts();

            connectionChecker = InternetConnectionChecker.GetInstance();

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

        /// <summary>
        /// Method used to receive an incoming ViewUpdatedMessage that prompts the ViewModel to refresh the list of
        /// available treasure hunts for a given administrator from the database. 
        /// </summary>
        /// <param name="updatedView"></param>
        private void ReceiveViewUpdatedMessage(bool updatedView)
        {
            if (updatedView)
            {
                RefreshTreasureHunts();
            }
        }

        /// <summary>
        /// Method used to receive an incoming CurrentUserMessage to store the data related to the current  
        /// user accessing the application 
        /// </summary>
        /// <param name="currentUser"></param>
        private void ReceiveCurrentUserMessage(user currentUser)
        {
            CurrentUser = currentUser;
        }
        #endregion

        #endregion

        #region Methods

        #region Validation

        /// <summary>
        /// Method to check whether or not a treasure hunt has been currently selected. 
        /// </summary>
        /// <returns></returns>
        public bool IsValidHunt()
        {
            if (currentTreasureHunt != null)
            {
                return true;
            }
            return false;
        }
        #endregion

        #region General methods

        /// <summary>
        /// Method that will pull down all of the available treasure hunts from the database for a given administrator. 
        /// </summary>
        public async void RefreshTreasureHunts()
        {
            PopupDisplayed = true;
            TreasureHunts = await this.serviceClient.GetTreasureHuntsForParticularUserAsync(this.currentUser);
            CurrentTreasureHunt = null;
            PopupDisplayed = false;
        }

        /// <summary>
        /// Method that will navigate the administrator on screen to a view displaying the details of the currently selected hunt. 
        /// </summary>
        private void ExecuteSearchHuntCommand()
        {
            PopupDisplayed = true;
            //(-http://etaktix.blogspot.co.uk/2013/01/check-if-internet-connection-is.html)
            if (connectionChecker.IsInternetConnected())
            {
                //Takes the user to the selected hunt page.
                Messenger.Default.Send<SelectedHuntMessage>(new SelectedHuntMessage() { CurrentHunt = this.currentTreasureHunt });
                //PopupDisplayed = false;
                Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "ViewHuntViewModel" });

            }
            else
            {
                MessageBoxResult messageBox = MessageBox.Show(connectionChecker.ShowConnectionErrorMessage());
            }

            PopupDisplayed = false;
        }

        /// <summary>
        /// Method that will navigate the administrator to a view where they can create a new treasure hunt. 
        /// </summary>
        private void ExecuteCreateHuntCommand()
        {
            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "CreateHuntViewModel" });
        }

        //Method that will log the administrator out of the application and send them back to the Login screen.
        private void ExecuteLogoutCommand()
        {
            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "LoginViewModel" });
        }

        /// <summary>
        /// Method that will navigate the administrator to a view where they can reset their company's password. 
        /// </summary>
        private void ExecuteResetCompanyPasswordCommand()
        {
            Messenger.Default.Send<CurrentUserMessage>(new CurrentUserMessage() { CurrentUser = this.CurrentUser });
            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "ResetCompanyPasswordViewModel" });
        }

        #endregion

        #endregion 
    }
}
