using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight.Messaging;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using TreasureHuntDesktopApplication.FullClient.Messages;
using TreasureHuntDesktopApplication.FullClient.Project_Utilities;
using TreasureHuntDesktopApplication.FullClient.TreasureHuntService;

namespace TreasureHuntDesktopApplication.FullClient.ViewModel
{
    public class LoginViewModel : ViewModelBase, IDataErrorInfo
    {
        #region Setup
        ITreasureHuntService serviceClient;

        public RelayCommand LoginUserCommand { get; private set; }
        public RelayCommand RegisterCommand { get; private set; }

        public LoginViewModel(ITreasureHuntService _serviceClient)
        {
            serviceClient = _serviceClient;

            LoginUserCommand = new RelayCommand(() => ExecuteLoginUserCommand(), () => IsValidDetails());
            RegisterCommand = new RelayCommand(() => ExecuteRegisterCommand());

        }
        #endregion

        #region Variables

        private String password;
        public String Password
        {
            get { return this.password; }
            set
            {
                this.password = value;
                RaisePropertyChanged("Password");
            }
        }

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

        #endregion

        #region Validation

        public int EmailMinLength
        {
            get
            {
                return 10;

            }
        }

        public int EmailMaxLength
        {
            get
            {
                return 30;

            }
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

        public bool IsValidDetails()
        {
            foreach (string property in ValidatedProperties)
                if (GetValidationMessage(property) != null)
                    return false;

            return true;
        }
        #endregion

        #region Commands

        public void ExecuteLoginUserCommand()
        { 
                user user = this.serviceClient.GetUser(this.emailAddress);

                if (user == null)
                {
      
                    MessageBoxResult messageBox = MessageBox.Show("User does not exist", "Invalid details");
                    EmailAddress = String.Empty;
                    Password = String.Empty;
                
                }
                else
                { 
                    userrole userRole = this.serviceClient.GetUserRole(user);
                    if (userRole.RoleId == 1)
                    {
                        if (String.Equals(user.Password, this.password, StringComparison.OrdinalIgnoreCase))
                        {
                            EmailAddress = String.Empty;
                            Password = String.Empty;
                            Messenger.Default.Send<CurrentUserMessage>(new CurrentUserMessage() { CurrentUser = user });
                            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "SearchHuntViewModel" });
                            Messenger.Default.Send<ViewUpdatedMessage>(new ViewUpdatedMessage() { UpdatedView = true });
                        }
                        else 
                        {
                            
                            MessageBoxResult messageBox = MessageBox.Show("Incorrect username or password", "Incorrect Details");
                            Password = String.Empty;
                        }
                    }
                    else 
                    {
                        MessageBoxResult messageBox = MessageBox.Show("You cannot access this application with your email address", "Invalid user for this application");
                        EmailAddress = String.Empty;
                        Password = String.Empty;
                     
                    }
                }
        }

        private void ExecuteRegisterCommand()
        {
            EmailAddress = String.Empty;
            Password = String.Empty;
            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "RegisterViewModel" });
        }

        #endregion

        #region IDataErrorInfo
        //-http://codeblitz.wordpress.com/2009/05/08/wpf-validation-made-easy-with-idataerrorinfo/
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
            "EmailAddress",
            "Password"
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
                case "Password":
                    {
                        result = ValidatePassword();
                        break;
                    }
            }

            return result;
        }

        private String ValidateEmailAddress()
        {
            if (Validation.IsNullOrEmpty(EmailAddress))
            {
                return "Email address cannot be empty!";
            }
            //-http://stackoverflow.com/questions/5342375/c-sharp-regex-email-validation
            if (!Validation.IsValidEmail(EmailAddress))
            {
                return "Email is in an invalid format";
            }
            if (!Validation.IsValidLength(EmailAddress, EmailMaxLength, EmailMinLength))
            {
                return "Email Address is an invalid length!";
            }

            return null;
        }

        private String ValidatePassword()
        {
            if (Validation.IsNullOrEmpty(Password))
            {
                return "Password cannot be empty!";
            }
            //-http://blog.magnusmontin.net/2013/08/26/data-validation-in-wpf/
           /* if (!Validation.IsValidCharacters(Password))
            {
                return "There are invalid characters";
            }
            if (!Validation.IsValidLength(Password, PasswordMaxLength, PasswordMinLength))
            {
                return "Password is an invalid length!";
            } */

            return null;
        }
        #endregion

        #region Other 
        //-http://stackoverflow.com/questions/6278720/wpf-toggle-panel-visibility
        public Object convert(object value)
        {
            return (Visibility)value == Visibility.Visible ? Visibility.Collapsed : Visibility.Visible;
        }
        #endregion
    }

  
}
