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

namespace TreasureHuntDesktopApplication.FullClient.ViewModel
{
    public class ResetPasswordViewModel : ViewModelBase, IDataErrorInfo
    {
         #region Setup
         ITreasureHuntService serviceClient;
         public RelayCommand ResetPasswordCommand { get; private set; }
         public RelayCommand BackCommand { get; private set; }

        public ResetPasswordViewModel(ITreasureHuntService _serviceClient)
        {
            serviceClient = _serviceClient;

            ResetPasswordCommand = new RelayCommand(() => ExecuteCheckAnswerAndResetCommand(), () => IsValidDetails());
            BackCommand = new RelayCommand(() => ExecuteBackCommand());

           Messenger.Default.Register<CurrentUserMessage>
           (

           this,
           (action) => ReceiveCurrentUserMessage(action.CurrentUser)

           );
           
        }

        private void ReceiveCurrentUserMessage(user currentUser)
        {
            CurrentUser = currentUser;
            SecurityAnswer = this.serviceClient.getUserSecurityAnswer(currentUser);
            CurrentSecurityQuestion = this.serviceClient.getUserSecurityQuestion(CurrentUser);
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

        #region Validation

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

        public bool IsValidDetails()
        {
            foreach (string property in ValidatedProperties)
                if (GetValidationMessage(property) != null)
                    return false;

            return true;
        }
        #endregion 

        #region Commands
        private void ExecuteBackCommand()
        {
            UserSubmittedAnswer = String.Empty;
            NewPassword = String.Empty;

            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "LoginViewModel" });
        }

        private void ExecuteCheckAnswerAndResetCommand()
        {
            //update the database
            this.serviceClient.updateUserPassword(currentUser, NewPassword);

            MessageBoxResult messageBox = MessageBox.Show("Your password has been updated.", "Updated password");
            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "LoginViewModel" });
        }
        #endregion

        #region IDataErrorInfo
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

        private String ValidateSubmittedAnswer()
        {
            if (Validation.IsNullOrEmpty(UserSubmittedAnswer))
            {
                return "Hunt name cannot be empty!";
            }
            if (!UserSubmittedAnswer.Equals(SecurityAnswer.Answer))
            {
                return "This is not the correct answer to your security question.";
            }
            
            return null;
        }

        private String ValidateNewPassword()
        {
            if (Validation.IsNullOrEmpty(NewPassword))
            {
                return "Password cannot be empty!";
            }
            //-http://blog.magnusmontin.net/2013/08/26/data-validation-in-wpf/
            if (!Validation.IsValidCharacters(NewPassword))
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
    }


}
