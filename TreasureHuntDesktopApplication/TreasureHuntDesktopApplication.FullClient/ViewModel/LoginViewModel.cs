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

//----------------------------------------------------------
//<copyright>
//</copyright>
//----------------------------------------------------------

namespace TreasureHuntDesktopApplication.FullClient.ViewModel
{
    /// <Summary> This is the ViewModel associated with the LoginView and is responsible for the interaction
    /// between the View and the Model to allow an administrator to log into the application. 
    /// See Dissertation Section 2.4.1.2 </Summary>
 
    public class LoginViewModel : ViewModelBase, IDataErrorInfo
    {
        #region Setup
        #region Fields    

        #region General global variables 
        private ITreasureHuntService serviceClient;

        public RelayCommand LoginUserCommand { get; private set; }
        public RelayCommand RegisterCommand { get; private set; }
        public RelayCommand ForgotPasswordCommand { get; private set; }

        private InternetConnectionChecker connectionChecker;
        #endregion

        #region Binding variables

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

        #endregion

        #region Constructor

        public LoginViewModel(ITreasureHuntService serviceClient)
        {
            this.serviceClient = serviceClient;

            LoginUserCommand = new RelayCommand(() => ExecuteLoginUserCommand(), () => IsValidDetails());
            RegisterCommand = new RelayCommand(() => ExecuteRegisterCommand());
            ForgotPasswordCommand = new RelayCommand(() => ExecuteForgotPasswordCommand());

            connectionChecker = InternetConnectionChecker.GetInstance();

            PopupDisplayed = false;
        }
        #endregion

        #endregion
   
        #region Methods
        #region General Methods

        /// <summary>
        /// Method that will navigate an administrator to a view where they can enter their email address to reset their 
        /// login password.
        /// </summary>
        public void ExecuteForgotPasswordCommand()
        {
            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "RetrieveEmailViewModel" });
            EmailAddress = String.Empty;
            Password = String.Empty;
        }

        /// <summary>
        /// Method that will attempt to log an administrator into the application.
        /// </summary>
        public async void ExecuteLoginUserCommand()
        {
            if (connectionChecker.IsInternetConnected())
            {
                PopupDisplayed = true;

                //Retrieve the user associated with the given email address.
                user user= await this.serviceClient.GetUserAsync(this.emailAddress);

                //Check if this user exists in the database
                if (user == null)
                {
                    PopupDisplayed = false;
                    MessageBoxResult messageBox = MessageBox.Show("User does not exist", "Invalid details");
                    EmailAddress = String.Empty;
                    Password = String.Empty;
                }
                else
                {
                    //If this user does exist
                    userrole userRole = await this.serviceClient.GetUserRoleAsync(user);
                    
                    //Check that they are an administrative type user as opposed to a participant type user. 
                    if (userRole.RoleId == 1)
                    {
                        checkDetailsForValidAdministrator(user);
                    }
                    else
                    {
                        PopupDisplayed = false;
                        MessageBoxResult messageBox = MessageBox.Show("You cannot access this application with your email address", "Invalid user for this application");
                        EmailAddress = String.Empty;
                        Password = String.Empty;
                    }
                }
            }
            else 
            {
                MessageBoxResult messageBox = MessageBox.Show(connectionChecker.ShowConnectionErrorMessage());
            }
        }

        /// <summary>
        /// Method that will check that the details supplied by a valid administrator match what is stored on the database
        /// for this particular administrator.
        /// </summary>
        /// <param name="user"></param>
        private void checkDetailsForValidAdministrator(user user)
        {
            if (String.Equals(user.Password, this.password, StringComparison.OrdinalIgnoreCase))
            {
                PopupDisplayed = false;
                EmailAddress = String.Empty;
                Password = String.Empty;
                Messenger.Default.Send<CurrentUserMessage>(new CurrentUserMessage() { CurrentUser = user });
                Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "SearchHuntViewModel" });
                Messenger.Default.Send<ViewUpdatedMessage>(new ViewUpdatedMessage() { UpdatedView = true });
            }
            else
            {
                PopupDisplayed = false;
                MessageBoxResult messageBox = MessageBox.Show("Incorrect username or password", "Incorrect Details");
                Password = String.Empty;
            }
        }

        /// <summary>
        /// Method that will navigate an adminstrator to the view where they can register with the application.
        /// </summary>
        private void ExecuteRegisterCommand()
        {
            EmailAddress = String.Empty;
            Password = String.Empty;
            Messenger.Default.Send<RegenerateListMessage>(new RegenerateListMessage() { RegenerateList = true });
            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "RegisterViewModel" });
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

        /// <summary>
        /// Method that returns the validation message (if any) for a given property.
        /// </summary>
        /// <param name="propertyName"></param>
        /// <returns></returns>
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

        /// <summary>
        /// Method that controls the validation of a given email address
        /// </summary>
        /// <returns></returns>
        private String ValidateEmailAddress()
        {
            if (Validation.IsNullOrEmpty(EmailAddress))
            {
                return "Email address cannot be empty.";
            }
            //-http://stackoverflow.com/questions/5342375/c-sharp-regex-email-validation
            if (!Validation.IsValidEmail(EmailAddress))
            {
                return "Email address must be in a valid email format.";
            }

            return null;
        }

        /// <summary>
        /// Method that controls the validation of a given password
        /// </summary>
        /// <returns></returns>
        private String ValidatePassword()
        {
            if (Validation.IsNullOrEmpty(Password))
            {
                return "Password cannot be empty.";
            }
            return null;
        }
        #endregion

        #endregion 
    }

  
}
