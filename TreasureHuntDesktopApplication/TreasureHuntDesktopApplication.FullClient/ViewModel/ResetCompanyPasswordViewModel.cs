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
    public class ResetCompanyPasswordViewModel : ViewModelBase, IDataErrorInfo
    {
        #region Setup

        ITreasureHuntService serviceClient;
        public RelayCommand ResetCompanyPasswordCommand { get; private set; }
        public RelayCommand BackCommand { get; private set; }

        public ResetCompanyPasswordViewModel(ITreasureHuntService _serviceClient)
        {
            serviceClient = _serviceClient;
            ResetCompanyPasswordCommand = new RelayCommand(() => ExecuteResetCompanyPasswordCommand(), () => IsValidDetails());
            BackCommand = new RelayCommand(() => ExecuteBackCommand());

            Messenger.Default.Register<CurrentUserMessage>
           (

           this,
           (action) => ReceiveCurrentUserMessage(action.CurrentUser)

           );
        }
        #endregion

        #region Received Messages

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

        public bool IsValidDetails()
        {
            foreach (string property in ValidatedProperties)
                if (GetValidationMessage(property) != null)
                    return false;

            return true;
        }

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

        #region Commands

        private void ExecuteBackCommand()
        {
            NewPassword = String.Empty;
            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "SearchHuntViewModel" });
            // DO I NEED TO SEND BACK THE USER ID HERE?
        }

        private void ExecuteResetCompanyPasswordCommand()
        { 
            this.serviceClient.updateCompanyPassword(currentUser, newPassword);
            MessageBoxResult messageBox = MessageBox.Show("Company password has been updated.", "Updated password");
            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "SearchHuntViewModel" });
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
                case "NewPassword":
                    {
                        result = ValidateNewPassword();
                        break;
                    }
            }

            return result;
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
