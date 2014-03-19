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
    public class RetrieveEmailViewModel : ViewModelBase, IDataErrorInfo
    {
        #region Setup
         ITreasureHuntService serviceClient;
         public RelayCommand CheckEmailAddressCommand { get; private set; }
         public RelayCommand BackCommand { get; private set; }

         public RetrieveEmailViewModel(ITreasureHuntService _serviceClient)
        {
            serviceClient = _serviceClient;

            CheckEmailAddressCommand = new RelayCommand(() => ExecuteCheckEmailAddressCommand(), () => IsValidDetails());
            BackCommand = new RelayCommand(() => ExecuteBackCommand());

            PopupDisplayed = false;
       
        }
        #endregion

         #region Variables

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

         #region Validation

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
             EmailAddress = String.Empty;

             Messenger.Default.Send<UpdateViewMessage>(new UpdateViewMessage() { UpdateViewTo = "LoginViewModel" });
         }

         private void ExecuteCheckEmailAddressCommand()
         {
             if (InternetConnectionChecker.IsInternetConnected())
             {
                 PopupDisplayed = true;
                 //Check here if the email address exists
                 user emailUser = this.serviceClient.GetUser(EmailAddress);
             
                 //if it exists then move to the next view with the user
                 if (emailUser != null)
                 {
                     userrole emailUserRole = this.serviceClient.GetUserRole(emailUser);

                     if (emailUserRole.RoleId == 2)
                     {
                         PopupDisplayed = false;
                         MessageBoxResult messageBox = MessageBox.Show("You cannot reset this email address on this application", "Invalid user");
                         EmailAddress = String.Empty;
                     }
                     else 
                     {
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
            MessageBoxResult messageBox = MessageBox.Show(InternetConnectionChecker.ShowConnectionErrorMessage());
        } 
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
             return null;
         }

         #endregion
    }
}
