using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight.Messaging;
using System;
using System.Collections.Generic;
using System.ComponentModel;
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
    /// <Summary> This is the ViewModel associated with the ResetCompanyPasswordView and is responsible for the interaction
    /// between the View and the Model to reset a particular administrator's company password. 
    /// See Dissertation Section 2.4.1.8.2 </Summary>
    
    public class ResetCompanyPasswordViewModel : ViewModelBase, IDataErrorInfo
    {
        #region Setup

        #region Fields

        #region General global variables
        private ITreasureHuntService serviceClient;
        public RelayCommand ResetCompanyPasswordCommand { get; private set; }
        public RelayCommand BackCommand { get; private set; }

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

        private String newPassword;
        public String NewPassword
        {
            get { return this.newPassword; }
            set
            {

                this.newPassword = value;
                RaisePropertyChanged("NewPassword");
            }
        }
        #endregion

        #region Validation variables

        public int PasswordMinLength
        {
            get
            {
                return 6;

            }
        }

        public int PasswordMaxLength
        {
            get
            {
                return 20;

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

        #endregion

        #endregion

        #region Constructor
        public ResetCompanyPasswordViewModel(ITreasureHuntService serviceClient)
        {
            this.serviceClient = serviceClient;
            ResetCompanyPasswordCommand = new RelayCommand(() => ExecuteResetCompanyPasswordCommand(), () => IsValidDetails());
            BackCommand = new RelayCommand(() => ExecuteBackCommand());

            connectionChecker = InternetConnectionChecker.GetInstance();

            Messenger.Default.Register<CurrentUserMessage>
           (

           this,
           (action) => ReceiveCurrentUserMessage(action.CurrentUser)

           );
        }
        #endregion

        #region Received Messages

        /// <summary>
        ///  Method used to receive an incoming CurrentUserMessage to store the data related to the current  
        ///  user accessing the application. 
        /// </summary>
        /// <param name="currentUser"></param>
        private void ReceiveCurrentUserMessage(user currentUser)
        {
            CurrentUser = currentUser;
        }

        #endregion

        #endregion

        #region Methods
       
        #region General Methods

        /// <summary>
        /// Method that will navigate the administrator back to the Homepage view.
        /// </summary>
        private void ExecuteBackCommand()
        {
            NewPassword = String.Empty;
            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "SearchHuntViewModel" });
        }

        /// <summary>
        /// Method that attempts to reset the password of the company associated with the current administrator. 
        /// </summary>
        public async void ExecuteResetCompanyPasswordCommand()
        {
            if (connectionChecker.IsInternetConnected())
            {
                PopupDisplayed = true;
                await this.serviceClient.updateCompanyPasswordAsync(currentUser, newPassword);
                PopupDisplayed = false;
                MessageBoxResult messageBox = MessageBox.Show("Company password has been updated.", "Updated password");
                Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "SearchHuntViewModel" });
            }
            else
            {
                MessageBoxResult messageBox = MessageBox.Show(connectionChecker.ShowConnectionErrorMessage());
            }
        }

        #endregion

        #region Validation 
        /// <summary>
        /// Method to determine whether or not all of the relevant properties are correct with 
        /// regards to their validation.
        /// </summary>
        /// <returns></returns>
        public bool IsValidDetails()
        {
            foreach (string property in ValidatedProperties)
                if (GetValidationMessage(property) != null)
                    return false;

            return true;
        }
        #endregion

        #region IDataErrorInfo validation methods
        //-http://codeblitz.wordpress.com/2009/05/08/wpf-validation-made-easy-with-idataerrorinfo/
        //-http://www.youtube.com/watch?v=OOHDie8BdGI 
        string IDataErrorInfo.Error
        {
            get
            {
                return null;
            }
        }

        //Properties to be validated
        static readonly string[] ValidatedProperties = 
        { 
            "NewPassword"
        };

        string IDataErrorInfo.this[string propertyName]
        {
            get
            {
                return GetValidationMessage(propertyName);
            }
        }

        /// <summary>
        ///  Method that returns the validation message (if any) for a given property.
        /// </summary>
        /// <param name="propertyName"></param>
        /// <returns></returns>
        private string GetValidationMessage(string propertyName)
        {
            String result = null;

            switch (propertyName)
            {
                case "NewPassword":
                    {
                            result = ValidateNewPassword();
                            break;
                        }
            }

            return result;
        }

        /// <summary>
        /// Method that controls the validation of a given password
        /// </summary>
        /// <returns></returns>
        private String ValidateNewPassword()
        {
            if (Validation.IsNullOrEmpty(NewPassword))
            {
                return "Password cannot be empty.";
            }
            //-http://blog.magnusmontin.net/2013/08/26/data-validation-in-wpf/
            if (!Validation.IsValidPasswordCharacters(NewPassword))
            {
                return "Password must be made up of only alphanumeric characters.";
            }
            if (!Validation.IsValidLength(NewPassword, PasswordMaxLength, PasswordMinLength))
            {
                return "Password must be between 6 and 20 characters.";
            }

            return null;
        }

        #endregion

        #endregion
    
    }
}
