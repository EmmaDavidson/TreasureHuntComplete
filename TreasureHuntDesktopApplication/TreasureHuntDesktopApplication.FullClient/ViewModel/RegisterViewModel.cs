﻿using GalaSoft.MvvmLight;
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
    public class RegisterViewModel : ViewModelBase, IDataErrorInfo
    {
        #region Setup
        ITreasureHuntService serviceClient;
         public RelayCommand RegisterUserCommand { get; private set; }
         public RelayCommand BackCommand { get; private set; }

        public RegisterViewModel(ITreasureHuntService _serviceClient)
        {

            serviceClient = _serviceClient;

            RegisterUserCommand = new RelayCommand(() => ExecuteRegisterUserCommand(), () => IsValidDetails());
            BackCommand = new RelayCommand(() => ExecuteBackCommand());

            Messenger.Default.Register<RegenerateListMessage>
             (

             this,
             (action) => ReceiveRegenerateListMessage(action.RegenerateList)

             );

            PopupDisplayed = false;
        }
        #endregion

        #region Receiving messages
        private void ReceiveRegenerateListMessage(bool updatedView)
        {
            if (updatedView)
            {
                if (InternetConnectionChecker.IsInternetConnected())
                {
                    SecurityQuestions = this.serviceClient.getListOfSecurityQuestionsAsync().Result.AsEnumerable();
                }
            }  
        }
        #endregion

        #region Variables
        private String name;
        public String Name
        {
            get { return this.name; }
            set
            {
                this.name = value;
                RaisePropertyChanged("Name");
            }
        }

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

        private String companyName;
        public String CompanyName
        {
            get { return this.companyName; }
            set
            {
                this.companyName = value;
                RaisePropertyChanged("CompanyName");
            }
        }

        private String companyPassword;
        public String CompanyPassword
        {
            get { return this.companyPassword; }
            set
            {
                this.companyPassword = value;
                RaisePropertyChanged("CompanyPassword");
            }
        }

        private IEnumerable<securityquestion> securityQuestions;
        public IEnumerable<securityquestion> SecurityQuestions
        {
            get { return this.securityQuestions; }
            set
            {
                this.securityQuestions = value;
                RaisePropertyChanged("SecurityQuestions");
            }
        }

        private String securityAnswer;
        public String SecurityAnswer
        {
            get { return this.securityAnswer; }
            set
            {
                this.securityAnswer = value;
                RaisePropertyChanged("SecurityAnswer");
            }
        }
        #endregion

        #region Validation

        public int NameMinLength
        {
            get
            {
                return 4;

            }
        }

        public int NameMaxLength
        {
            get
            {
                return 30;

            }
        }

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

        public int CompanyNameMaxLength
        {
            get
            {
                return 30;

            }
        }

        public int CompanyNameMinLength
        {
            get
            {
                return 4;

            }
        }

        public int SecurityAnswerMinLength
        {
            get
            {
                return 3;

            }
        }

        public int SecurityAnswerMaxLength
        {
            get
            {
                return 30;

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

        public async void ExecuteRegisterUserCommand()
        {
            if (InternetConnectionChecker.IsInternetConnected())
            {
                PopupDisplayed = true;
                user newUser = new user();
                newUser.Email = this.emailAddress;
                newUser.Password = this.password;
                newUser.Name = this.name;

                if (!DoesUserOrCompanyAlreadyExist(newUser.Email, this.companyName))
                {
                    long userId = await this.serviceClient.SaveUserAsync(newUser);

                    companydetail newCompany = new companydetail();
                    newCompany.UserId = userId;
                    newCompany.CompanyName = this.companyName;
                    newCompany.CompanyPassword = this.companyPassword;

                    await this.serviceClient.saveCompanyAsync(newCompany);

                    userrole newUserRole = new userrole();
                    newUserRole.UserId = userId;
                    newUserRole.RoleId = 1;

                    await this.serviceClient.SaveUserRoleAsync(newUserRole);

                    //Saving the security question for this user
                    usersecurityquestion newSecurityQuestion = new usersecurityquestion();
                    newSecurityQuestion.UserId = userId;
                    newSecurityQuestion.SecurityQuestionId = CurrentSecurityQuestion.SecurityQuestionId;
                    newSecurityQuestion.Answer = SecurityAnswer;
                    await this.serviceClient.SaveUserSecurityQuestionAsync(newSecurityQuestion);

                    PopupDisplayed = false;
         
                    MessageBoxResult messageBox = MessageBox.Show("You have successfully registered!", "Success");

                    Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "LoginViewModel" });

                    EmailAddress = String.Empty;
                    Password = String.Empty;
                    Name = String.Empty;
                    CompanyName = String.Empty;
                    CompanyPassword = String.Empty;
                    securityAnswer = String.Empty ;
                }
            }
            else
            {
                PopupDisplayed = false;
                MessageBoxResult messageBox = MessageBox.Show(InternetConnectionChecker.ShowConnectionErrorMessage());
            }
        }     

        private void ExecuteBackCommand()
        {
            EmailAddress = String.Empty;
            Password = String.Empty;
            Name = String.Empty;
            CompanyName = String.Empty;
            CompanyPassword = String.Empty;
            securityAnswer = String.Empty;

            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "LoginViewModel" });
        }

        private bool DoesUserOrCompanyAlreadyExist(string emailAddress, string company)
        {
            List<user> listOfUsers = serviceClient.GetExistingUsersAsync().Result.ToList();
            List<companydetail> listOfCompanies = serviceClient.getExistingCompaniesAsync().Result.ToList();

            bool userExists = false;
            bool companyExists = false;

            if (listOfUsers != null)
            {
                using (var currentUsers = listOfUsers.GetEnumerator())
                {
                    while (currentUsers.MoveNext())
                    {
                        if (String.Equals(currentUsers.Current.Email, emailAddress, StringComparison.OrdinalIgnoreCase))
                        {
                            PopupDisplayed = false;
                            MessageBoxResult messageBox = MessageBox.Show("This email address already exists!", "Invalid details");
                            EmailAddress = String.Empty;
                            userExists = true;
                        }     
                    }
                }
            }

            if (listOfCompanies != null)
            {
                using (var currentCompanies = listOfCompanies.GetEnumerator())
                {
                    while (currentCompanies.MoveNext())
                    {
                        if (String.Equals(currentCompanies.Current.CompanyName, company, StringComparison.OrdinalIgnoreCase))
                        {
                            PopupDisplayed = false;
                            //-http://www.c-sharpcorner.com/UploadFile/mahesh/messagebox-in-wpf/
                            MessageBoxResult messageBox = MessageBox.Show("This company already exists!", "Invalid details");
                            CompanyName = String.Empty;
                            companyExists = true;
                        }
                    }
                }
            }

            if (!companyExists && !userExists)
            {
                return false;
            }
            return true;
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
            "Name",
            "EmailAddress",
            "Password",
            "CompanyName",
            "CompanyPassword",
            "SecurityAnswer",
            "CurrentSecurityQuestion"
            
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
                case "Name":
                    {
                        result = ValidateName();
                        break;
                    }
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
                case "CurrentSecurityQuestion":
                    {
                        result = ValidateCurrentSecurityQuestion();
                        break;
                    }
                case "CompanyName":
                    {
                        result = ValidateCompanyName();
                        break;
                    }
                case "CompanyPassword":
                    {
                        result = ValidateCompanyPassword();
                        break;
                    }
                case "SecurityAnswer":
                    {
                        result = ValidateSecurityAnswer();
                        break;
                    }
            }

            return result;
        }

        private String ValidateName()
        {
            if (Validation.IsNullOrEmpty(Name))
            {
                return "Name cannot be empty!";
            }
            //-http://blog.magnusmontin.net/2013/08/26/data-validation-in-wpf/
            if (!Validation.IsValidCharacters(Name))
            {
                return "There are invalid characters";
            }
            if (!Validation.IsValidLength(Name, NameMaxLength, NameMinLength))
            {
                return "Name is an invalid length!";
            }

            return null;
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
                return "Email address is in an invalid format";
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
            if (!Validation.IsValidCharacters(Password))
            {
                return "There are invalid characters";
            }
            if (!Validation.IsValidLength(Password, PasswordMaxLength, PasswordMinLength))
            {
                return "Password is an invalid length!";
            }

            return null;
        }

        /*private String ValidateMatchingPasswords()
        {
            if (Validation.IsNullOrEmpty(RetypedPassword))
            {
                return "This field cannot be empty!";
            }
            //-http://blog.magnusmontin.net/2013/08/26/data-validation-in-wpf/
            if (!Validation.IsValidCharacters(RetypedPassword))
            {
                return "There are invalid characters";
            }
            if (!Validation.ArePasswordsMatching(Password, RetypedPassword))
            {
                return "Passwords do not match";
            }

            return null;
        }*/

        private String ValidateCompanyName()
        {
            if (Validation.IsNullOrEmpty(CompanyName))
            {
                return "Company Name cannot be empty!";
            }
            //-http://blog.magnusmontin.net/2013/08/26/data-validation-in-wpf/
            if (!Validation.IsValidCharacters(CompanyName))
            {
                return "There are invalid characters";
            }
            if (!Validation.IsValidLength(CompanyName, CompanyNameMaxLength, CompanyNameMinLength))
            {
                return "Company Name is an invalid length!";
            }

            return null;
        }

        private String ValidateCompanyPassword()
        {
            if (Validation.IsNullOrEmpty(CompanyPassword))
            {
                return "Password cannot be empty!";
            }
            //-http://blog.magnusmontin.net/2013/08/26/data-validation-in-wpf/
            if (!Validation.IsValidCharacters(CompanyPassword))
            {
                return "There are invalid characters";
            }
            if (!Validation.IsValidLength(Password, PasswordMaxLength, PasswordMinLength))
            {
                return "Password is an invalid length!";
            }

            return null;
        }

        private String ValidateSecurityAnswer()
        {

            if (Validation.IsNullOrEmpty(SecurityAnswer))
            {
                return "Answer cannot be empty!";
            }
            //-http://blog.magnusmontin.net/2013/08/26/data-validation-in-wpf/
            if (!Validation.IsValidCharacters(SecurityAnswer))
            {
                return "There are invalid characters";
            }
            if (!Validation.IsValidLength(SecurityAnswer, SecurityAnswerMaxLength, SecurityAnswerMinLength))
            {
                return "Answer is an invalid length!";
            }

            return null;   
        }

        private String ValidateCurrentSecurityQuestion()
        {
            if (CurrentSecurityQuestion == null)
            {
                return "Please choose a question.";
            }
            return null;
        }
        #endregion
    }
}
