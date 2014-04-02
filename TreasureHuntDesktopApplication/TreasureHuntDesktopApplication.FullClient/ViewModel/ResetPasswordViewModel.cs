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
    /// <Summary> This is the ViewModel associated with the ResetPasswordView and is responsible for the interaction
    /// between the View and the Model to reset a particular administrator's login password.
    /// See Disseration Section 2.4.1.8.1 </Summary>
   
    public class ResetPasswordViewModel : ViewModelBase, IDataErrorInfo
    {
        #region Setup

        #region Fields

        #region General global variables
        private ITreasureHuntService serviceClient;
        public RelayCommand ResetPasswordCommand { get; private set; }
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

        private securityquestion currentSecurityQuestion;
        public securityquestion CurrentSecurityQuestion
        {
            get { return this.currentSecurityQuestion; }
            set
            {
                this.currentSecurityQuestion = value;
                RaisePropertyChanged("CurrentSecurityQuestion");
            }
        }

        private usersecurityquestion securityAnswer;
        public usersecurityquestion SecurityAnswer
        {
            get { return this.securityAnswer; }
            set
            {
                this.securityAnswer = value;
                RaisePropertyChanged("SecurityAnswer");
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

        private String userSubmittedAnswer;
        public String UserSubmittedAnswer
        {
            get { return this.userSubmittedAnswer; }
            set
            {
                this.userSubmittedAnswer = value;
                RaisePropertyChanged("UserSubmittedAnswer");
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

        #endregion 

        #endregion

        #region Constructor
        public ResetPasswordViewModel(ITreasureHuntService serviceClient)
         {
            this.serviceClient = serviceClient;

            ResetPasswordCommand = new RelayCommand(() => ExecutePasswordResetCommand(), () => IsValidDetails());
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
        /// Method used to receive an incoming CurrentUserMessage to store the data related to the current  
        /// user accessing the application. It also returns from the database the security question and answer associated
        /// with this given administrator.
        /// </summary>
        /// <param name="currentUser"></param>
        private async void ReceiveCurrentUserMessage(user currentUser)
        {
            CurrentUser = currentUser;
            SecurityAnswer = await this.serviceClient.getUserSecurityAnswerAsync(currentUser);
            CurrentSecurityQuestion = await this.serviceClient.getUserSecurityQuestionAsync(CurrentUser);
        }
        #endregion 

        #endregion

        #region Methods

        #region General methods

        /// <summary>
        /// Method that navigates the administrator back to the Login screen.
        /// </summary>
        private void ExecuteBackCommand()
        {
            UserSubmittedAnswer = String.Empty;
            NewPassword = String.Empty;

            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "LoginViewModel" });
        }

        /// <summary>
        /// Method that attempts to reset the administrator's password with the new password submitted on screen.
        /// </summary>
        public async void ExecutePasswordResetCommand()
        {
            if (connectionChecker.IsInternetConnected())
            {
                PopupDisplayed = true;

                await this.serviceClient.updateUserPasswordAsync(currentUser, NewPassword);

                PopupDisplayed = false;
                MessageBoxResult messageBox = MessageBox.Show("Your password has been updated.", "Updated password");
                Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "LoginViewModel" });
            }
            else
            {
                MessageBoxResult messageBox = MessageBox.Show(connectionChecker.ShowConnectionErrorMessage());
            }
        }
        #endregion

        #region Validation
        /// <summary>
        ///  Method to determine whether or not all of the relevant properties are correct with 
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
            "UserSubmittedAnswer",
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
                case "UserSubmittedAnswer":
                    {
                            result = ValidateSubmittedAnswer();
                            break;
                         }
                case "NewPassword":
                    {
                            result = ValidateNewPassword();
                            break;
                        }

            }

            return result;
        }

        /// <summary>
        /// Method that controls the validation of a given answer
        /// </summary>
        /// <returns></returns>
        private String ValidateSubmittedAnswer()
        {
            if (Validation.IsNullOrEmpty(UserSubmittedAnswer))
            {
                return "Hunt name cannot be empty!";
            }
            //Check to see whether or not the answer submitted on screen matches the answer saved in the database 
            //for the given administrator. 
            if (!UserSubmittedAnswer.Equals(SecurityAnswer.Answer))
            {
                return "This is not the correct answer to your security question.";
            }
            
            return null;
        }

        /// <summary>
        /// Method that controls the validation of a given password
        /// </summary>
        /// <returns></returns>
        private String ValidateNewPassword()
        {
            if (Validation.IsNullOrEmpty(NewPassword))
            {
                return "Password cannot be empty!";
            }
            //-http://blog.magnusmontin.net/2013/08/26/data-validation-in-wpf/
            if (!Validation.IsValidPasswordCharacters(NewPassword))
            {
                return "There are invalid characters";
            }
            if (!Validation.IsValidLength(NewPassword, PasswordMaxLength, PasswordMinLength))
            {
                return "Password is an invalid length!";
            }

            return null;
        }

        #endregion
        #endregion
    }

}
