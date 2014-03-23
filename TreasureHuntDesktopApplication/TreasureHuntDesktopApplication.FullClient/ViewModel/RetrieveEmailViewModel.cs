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
//</copyright>
//----------------------------------------------------------

namespace TreasureHuntDesktopApplication.FullClient.ViewModel
{
    /// <Summary> This is the ViewModel associated with the RetrieveEmailView and is responsible for gathering
    /// a valid adminstrative email address prior to resetting the associated login password. </Summary>

    public class RetrieveEmailViewModel : ViewModelBase, IDataErrorInfo
    {
        #region Setup

        #region Fields

        #region General global variables
        private ITreasureHuntService serviceClient;
        public RelayCommand CheckEmailAddressCommand { get; private set; }
        public RelayCommand BackCommand { get; private set; }

        private InternetConnectionChecker connectionChecker;
        #endregion 

        #region Binding variables

        private String emailAddress;
        public String EmailAddress
        {
            get { return this.emailAddress; }
            set
            {
                this.emailAddress = value;
                RaisePropertyChanged("EmailAddress");
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
        public RetrieveEmailViewModel(ITreasureHuntService serviceClient)
         {
            this.serviceClient = serviceClient;

            CheckEmailAddressCommand = new RelayCommand(() => ExecuteCheckEmailAddressCommand(), () => IsValidDetails());
            BackCommand = new RelayCommand(() => ExecuteBackCommand());

            connectionChecker = InternetConnectionChecker.GetInstance();

            PopupDisplayed = false;
       
         }
        #endregion

        #endregion

        #region Methods

        #region General methods

        /// <summary>
        /// Method that will navigate the administrator back to the Login screen.
        /// </summary>
        private void ExecuteBackCommand()
         {
             EmailAddress = String.Empty;

             Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "LoginViewModel" });
         }

        /// <summary>
        /// Method that attempts to check if the email address supplied on screen exists in the database and is associated with
        /// an administrative user. 
        /// </summary>
        private void ExecuteCheckEmailAddressCommand()
         {
             if (connectionChecker.IsInternetConnected())
             {
                 PopupDisplayed = true;
                 
                 //Grab the user associated with the given email address
                 user emailUser = this.serviceClient.GetUser(EmailAddress);

                 if (emailUser != null)
                 {
                     //If the user exists, then check to see if it is an administrative type user. 
                     userrole emailUserRole = this.serviceClient.GetUserRole(emailUser);

                     if (emailUserRole.RoleId == 2)
                     {
                         PopupDisplayed = false;
                         MessageBoxResult messageBox = MessageBox.Show("You cannot reset this email address on this application", "Invalid user");
                         EmailAddress = String.Empty;
                     }
                     else
                     {
                         //If the user is an administrator then direct them to the Reset Password view.
                         PopupDisplayed = false;
                         Messenger.Default.Send<CurrentUserMessage>(new CurrentUserMessage() { CurrentUser = emailUser });
                         Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "ResetPasswordViewModel" });
                         EmailAddress = String.Empty;
                     }
                 }
                 else
                 {
                     PopupDisplayed = false;
                     MessageBoxResult messageBox = MessageBox.Show("This email address does not exist.", "Invalid email address");
                     EmailAddress = String.Empty;
                 }
             }
             else
             {
                 MessageBoxResult messageBox = MessageBox.Show(connectionChecker.ShowConnectionErrorMessage());
             }
         }

         #endregion

        #region Validation methods

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

         //What properties I am validating.
         static readonly string[] ValidatedProperties = 
        { 
            "EmailAddress"
        };

         string IDataErrorInfo.this[string propertyName]
         {
             get
             {
                 return GetValidationMessage(propertyName);
             }
         }

         private string GetValidationMessage(string propertyName)
         {
             String result = null;

             switch (propertyName)
             {
                 case "EmailAddress":
                     {
                         result = ValidateEmailAddress();
                         break;
                     }
             }

             return result;
         }

         private String ValidateEmailAddress()
         {
             if (Validation.IsNullOrEmpty(EmailAddress))
             {
                 return "Email address cannot be empty";
             }
             if (!Validation.IsValidEmail(EmailAddress))
             {
                 return "Email address format is invalid";
             }
             return null;
         }

         #endregion
        #endregion 
    }
}
