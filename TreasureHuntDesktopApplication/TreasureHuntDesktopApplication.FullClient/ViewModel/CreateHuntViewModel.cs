using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight.Messaging;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Windows;
using TreasureHuntDesktopApplication.FullClient.Messages;
using TreasureHuntDesktopApplication.FullClient.Project_Utilities;
using TreasureHuntDesktopApplication.FullClient.TreasureHuntService;

namespace TreasureHuntDesktopApplication.FullClient.ViewModel
{
    public class CreateHuntViewModel : ViewModelBase, IDataErrorInfo
    {
        #region Setup
        ITreasureHuntService serviceClient;
        public RelayCommand SaveHuntNameCommand { get; private set; }
        public RelayCommand BackCommand { get; private set; }
        public RelayCommand LogoutCommand { get; private set; }

        public CreateHuntViewModel(ITreasureHuntService _serviceClient)
        {
            serviceClient = _serviceClient;
            SaveHuntNameCommand = new RelayCommand(() => ExecuteSaveHuntNameCommand(), () => IsValidDetails());
            BackCommand = new RelayCommand(() => ExecuteBackCommand());
            LogoutCommand = new RelayCommand(() => ExecuteLogoutCommand());


            Messenger.Default.Register<CurrentUserMessage>
            (

            this,
            (action) => ReceiveCurrentUserMessage(action.CurrentUser)

            );
        }
        #endregion

        #region Receiving Messages

        private void ReceiveCurrentUserMessage(user currentUser)
        {
            CurrentUser = currentUser;
        }
        #endregion

        #region Variable getters and setters

        private String description;
        public String Description
        {
            get { return this.description; }
            set
            {
                this.description = value;
                RaisePropertyChanged("Description");
            }
        }

        //-http://stackoverflow.com/questions/20659070/wpf-datepicker-returns-previously-selected-date-using-mvvm
        private Nullable<DateTime> endDate;
        public Nullable<DateTime> EndDate
        {
            get
            {
                if (endDate == null)
                {
                    endDate = DateTime.Today;
                }

                return endDate;
            }

            set 
            {
                endDate = value;
                RaisePropertyChanged("EndDate");

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

        private String retypedPassword;
        public String RetypedPassword
        {
            get { return this.retypedPassword; }
            set
            {
                this.retypedPassword = value;
                RaisePropertyChanged("RetypedPassword");
            }
        }

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

        private string huntName;
        public string HuntName
        {
            get { return this.huntName; }
            set
            {

                this.huntName = value;
                RaisePropertyChanged("HuntName");
            }
        }
        #endregion

        #region Validation

        public int DescriptionMaxLength
        {
            get
            {
                return 1000;
            }
        }

        public int DescriptionMinLength
        {
            get
            {
                return 10;
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
                return 30;
            }
        }

        public int HuntNameMaxLength
        {
            get 
            {
                return 100;
            }
        }

        public int HuntNameMinLength
        {
            get 
            {
                return 5;
            }
        }

        //-http://www.youtube.com/watch?v=OOHDie8BdGI
        public bool IsValidDetails()
        {
            foreach(string property in ValidatedProperties)
                if(GetValidationMessage(property) != null)
                return false;
      
            return true;
        }
        #endregion

        #region Commands
        //Change this to private and use reflection for testing
        public void ExecuteSaveHuntNameCommand()
        {
            if (!DoesHuntAlreadyExist(HuntName))
            {
                hunt newHunt = new hunt();
                newHunt.HuntName = this.huntName;
                newHunt.Password = this.Password;
                newHunt.HuntDescription = this.Description;
                newHunt.EndDate = EndDate;

                long huntId = this.serviceClient.SaveNewHunt(newHunt);

                userrole newUserRole = this.serviceClient.GetUserRole(this.currentUser);

                userhunt newUserHunt = new userhunt();
                newUserHunt.HuntId = huntId;
                newUserHunt.UserId = this.currentUser.UserId;
                newUserHunt.UserRoleId = newUserRole.UserRoleId;

                this.serviceClient.SaveUserHunt(newUserHunt);
                
                //Grabs the correct hunt's ID and passes it into the view hunt view.
                //Ensures that the hunt has been saved to the database before it goes and grab's it
                hunt huntToView = serviceClient.GetHuntBasedOnName(newHunt.HuntName);

                Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "ViewHuntViewModel" });
                Messenger.Default.Send<SelectedHuntMessage>(new SelectedHuntMessage() { CurrentHunt = huntToView });
                Messenger.Default.Send<ViewUpdatedMessage>(new ViewUpdatedMessage() { UpdatedView = true });

                HuntName = null;
                Password = null;
                RetypedPassword = null;
                Description = null;
            }
            else 
            {
                String messageBoxText = "This hunt already exists in the database.";
                String caption = "Hunt Already Exists";
                MessageBoxResult box = MessageBox.Show(messageBoxText, caption);
                HuntName = null;
            }
        }

        private bool DoesHuntAlreadyExist(string newQuestion)
        {
            //GetHuntQuestions
            List<hunt> listOfHunts = serviceClient.GetTreasureHunts().ToList();

            using (var currentHunts = listOfHunts.GetEnumerator())
            {
                while (currentHunts.MoveNext())
                {
                    //-http://stackoverflow.com/questions/6371150/comparing-two-strings-ignoring-case-in-c-sharp
                    if(String.Equals(currentHunts.Current.HuntName, this.huntName, StringComparison.OrdinalIgnoreCase))
                    {
                        return true;
                    }
                }
            }

            return false;
        }

        private void ExecuteBackCommand()
        {
            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "SearchHuntViewModel" });
            HuntName = null;
            Password = String.Empty;
            RetypedPassword = String.Empty;
            Description = String.Empty;
        }

        private void ExecuteLogoutCommand()
        {

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
            "HuntName",
            "Password",
            "RetypedPassword",
            "Description"
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
                case "HuntName":
                    {
                        result = ValidateHuntName();
                        break;
                    }
                case "Password":
                    {
                        result = ValidatePassword();
                        break;
                    }
                case "RetypedPassword":
                    {
                        result = ValidateMatchingPasswords();
                        break;
                    }
                case "Description":
                    {
                        result = ValidateDescription();
                        break;
                    }

            }

            return result;
        }

        private String ValidateHuntName()
        {
            if (Validation.IsNullOrEmpty(HuntName))
            {
                return "Hunt name cannot be empty!";
            }
            if (!Validation.IsValidCharacters(HuntName))
            {
                return "There are invalid characters";
            }
            if (!Validation.IsValidLength(HuntName, HuntNameMaxLength, HuntNameMinLength))
            {
                return "Hunt name is an invalid length!";
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

        private String ValidateMatchingPasswords()
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
        }

        private String ValidateDescription()
        {
            if (Validation.IsNullOrEmpty(Description))
            {
                return "This field cannot be empty!";
            }
            if (!Validation.IsValidLength(Description, DescriptionMaxLength, DescriptionMinLength))
            {
                return "Description is an invalid length!";
            }
            return null;
        }

        #endregion
    }
}
