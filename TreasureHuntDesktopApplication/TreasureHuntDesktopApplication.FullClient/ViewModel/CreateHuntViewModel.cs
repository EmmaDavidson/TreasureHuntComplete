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

//----------------------------------------------------------
//<copyright>
/*
 * Emma Davidson - Treasure Hunt 2013-3014 Final Year Project
 */
//</copyright>
//----------------------------------------------------------

namespace TreasureHuntDesktopApplication.FullClient.ViewModel
{
    /// <Summary> This is the ViewModel associated with the CreateHuntView and is responsible for the interaction
    /// between the View and the Model for the creation of treasure hunts within this application. 
    /// See Dissertation Section 2.4.1.3 </Summary>

    public class CreateHuntViewModel : ViewModelBase, IDataErrorInfo
    {
        #region Setup

        #region Fields

        #region General global variables 
        private ITreasureHuntService serviceClient;
        public RelayCommand SaveHuntCommand { get; private set; }
        public RelayCommand BackCommand { get; private set; }

        private InternetConnectionChecker connectionChecker;
        #endregion

        #region Binding variables

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

        #region Validation variables

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

        public int HuntNameMaxLength
        {
            get
            {
                return 30;
            }
        }

        public int HuntNameMinLength
        {
            get
            {
                return 5;
            }
        }

        #endregion
       
        #region Constructor
        public CreateHuntViewModel(ITreasureHuntService serviceClient)
        {
            this.serviceClient = serviceClient;
            SaveHuntCommand = new RelayCommand(() => ExecuteSaveCommand(), () => IsValidDetails());
            BackCommand = new RelayCommand(() => ExecuteBackCommand());

            connectionChecker = InternetConnectionChecker.GetInstance();

            Messenger.Default.Register<CurrentUserMessage>
            (

            this,
            (action) => ReceiveCurrentUserMessage(action.CurrentUser)

            );

            PopupDisplayed = false;
        }
        #endregion

        #region Received Messages
        
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

        #endregion

        #region Methods

        #region General Methods
        /// <Summary> Method that attempts to check if a new hunt can be saved. </Summary>
        public void ExecuteSaveCommand() {
            if (connectionChecker.IsInternetConnected())
            {
                PopupDisplayed = true; 

                //If the hunt does not already exist.
                if (!DoesHuntAlreadyExist())
                {
                    //Save the treasure hunt to the database.
                    SaveNewHunt();
                }
                else
                {
                    PopupDisplayed = false;
                    String messageBoxText = "This hunt already exists in the database!";
                    String caption = "Hunt Already Exists";
                    MessageBoxResult box = MessageBox.Show(messageBoxText, caption);
                    HuntName = null;
                }
            }
            else
            {
                MessageBoxResult messageBox = MessageBox.Show(connectionChecker.ShowConnectionErrorMessage());
            }
        }

        /// <Summary> Method that saves a new hunt to the database. </Summary>
        private async void SaveNewHunt()
        { 
            hunt newHunt = new hunt();
            newHunt.HuntName = this.huntName;
            newHunt.HuntDescription = this.Description;
            newHunt.EndDate = EndDate;

            //Create it and save it to the database.
            long huntId = await this.serviceClient.SaveNewHuntAsync(newHunt);

            userhunt newUserHunt = new userhunt();
            newUserHunt.HuntId = huntId;
            newUserHunt.UserId = this.currentUser.UserId;

            await this.serviceClient.SaveUserHuntAsync(newUserHunt);

            //Grab the correct hunt's ID and pass it into the view hunt view.
            hunt huntToView = await serviceClient.GetHuntBasedOnNameAsync(newHunt.HuntName, currentUser.UserId);

            PopupDisplayed = false;

            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "ViewHuntViewModel" });
            Messenger.Default.Send<SelectedHuntMessage>(new SelectedHuntMessage() { CurrentHunt = huntToView });
            Messenger.Default.Send<ViewUpdatedMessage>(new ViewUpdatedMessage() { UpdatedView = true });

            HuntName = null;
            Description = null;
            EndDate = DateTime.Today;
        }

        /// <Summary> Method to check whether or not the treasure hunt to be saved already exists in the database.  </Summary>
        public bool DoesHuntAlreadyExist()
        {
            List<hunt> listOfUserHunts = serviceClient.GetTreasureHuntsForParticularUserAsync(currentUser).Result.ToList();

            using (var currentHunts = listOfUserHunts.GetEnumerator())
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

        /// <Summary> Method to direct the administrator back to the homepage. </Summary>
        private void ExecuteBackCommand()
        {
            Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "SearchHuntViewModel" });
            HuntName = null;
            Description = String.Empty;
            //-http://stackoverflow.com/questions/3090198/reset-value-of-wpf-tookkit-datepicker-to-default-value
            endDate = DateTime.Now;
        }

        #endregion

        #region Validation
        //-http://www.youtube.com/watch?v=OOHDie8BdGI
        /// <Summary> Method to determine whether or not all of the relevant properties are correct with 
        /// regards to their validation. </Summary>
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
            "HuntName",
            "Description",
            "EndDate"
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
                case "HuntName":
                    {
                            result = ValidateHuntName();
                            break;
                        }
                case "Description":
                    {
                            result = ValidateDescription();
                            break;
                        }
                case "EndDate":
                    {
                            result = ValidateEndDate();
                            break;
                        }
            }

            return result;
        }

        /// <summary>
        /// Method that controls the validation of a given hunt name
        /// </summary>
        /// <returns></returns>
        private String ValidateHuntName()
        {
            if (Validation.IsNullOrEmpty(HuntName))
            {
                return "Hunt name cannot be empty.";
            }
            if (!Validation.IsValidCharacters(HuntName))
            {
                return "Hunt name must be made up of only alphabetic characters .";
            }
            if (!Validation.IsValidLength(HuntName, HuntNameMaxLength, HuntNameMinLength))
            {
                return "Hunt name must be between 5 and 30 characters.";
            }          
            return null;
        }

        /// <summary>
        /// Method that controls the validation of a given hunt description
        /// </summary>
        /// <returns></returns>
        private String ValidateDescription()
        {
            if (Validation.IsNullOrEmpty(Description))
            {
                return "Hunt Description cannot be empty.";
            }
            if (!Validation.IsValidLength(Description, DescriptionMaxLength, DescriptionMinLength))
            {
                return "Hunt Description must be between 10 and 1000 characters.";
            }
            return null;
        }

        private String ValidateEndDate()
        {
            if (EndDate.Value <= DateTime.Today)
            {
                return "End date must not be before or equal to today's date";
            }
                
            return null;
        }

        #endregion
        #endregion
    }
}
