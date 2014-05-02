﻿using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight.Messaging;
using System;
using System.ComponentModel;
using System.Windows.Input;
using TreasureHuntDesktopApplication.FullClient.Messages;
using TreasureHuntDesktopApplication.FullClient.TreasureHuntService;

//----------------------------------------------------------
//<copyright>
/*
 * Emma Davidson - Treasure Hunt 2013-3014 Final Year Project. Originally generated by MVVM Light and adapted.
 */
//</copyright>
//----------------------------------------------------------

namespace TreasureHuntDesktopApplication.FullClient.ViewModel
{
    /// <Summary> This is the ViewModel associated with the MainWindow and is responsible for determining the navigation
    /// between the different Views available in the application.</Summary>
 
    public class MainViewModel : ViewModelBase
    {
        //-http://www.codeproject.com/Articles/72724/Beginning-a-WPF-MVVM-application-Navigating-betwee
        #region Setup
        #region Fields
        #region General global variables

        private ViewModelBase currentViewModel;
        private static readonly TreasureHuntServiceClient serviceClient = new TreasureHuntServiceClient();

        private readonly static CreateHuntViewModel createHuntViewModel = new CreateHuntViewModel(serviceClient);
        private readonly static ViewHuntViewModel viewHuntViewModel = new ViewHuntViewModel(serviceClient);
        private readonly static SearchHuntViewModel searchHuntViewModel = new SearchHuntViewModel(serviceClient);
        private readonly static PrintViewModel printViewModel = new PrintViewModel(serviceClient);
        private readonly static LoginViewModel loginViewModel = new LoginViewModel(serviceClient);
        private readonly static RegisterViewModel registerViewModel = new RegisterViewModel(serviceClient);
        private readonly static LeaderboardViewModel leaderboardViewModel = new LeaderboardViewModel(serviceClient);
        private readonly static RetrieveEmailViewModel retrieveEmailViewModel = new RetrieveEmailViewModel(serviceClient);
        private readonly static ResetPasswordViewModel resetPasswordViewModel = new ResetPasswordViewModel(serviceClient);
        private readonly static ResetCompanyPasswordViewModel resetCompanyPasswordViewModel = new ResetCompanyPasswordViewModel(serviceClient);
        #endregion 

        #region Binding variables
        public ViewModelBase CurrentViewModel
        {
            get
            {
                return currentViewModel;
            }
            set
            {
                if (currentViewModel == value)
                    return;
                currentViewModel = value;
                RaisePropertyChanged("CurrentViewModel");
            }
        }
        #endregion 

        #endregion

        #region Constructor
        public MainViewModel()
        {
            CurrentViewModel = MainViewModel.loginViewModel;

            Messenger.Default.Register<UpdateViewMessage>
                (
                
                this,
                (action) => ReceiveViewMessage(action.UpdateViewTo)
                
                );
        }
        #endregion 

        #region Received Messages

        /// <summary>
        /// Method that received an UpdateViewMessage that will determine what view the screen should currently show.  
        /// </summary>
        /// <param name="requestedUpdateViewModel"></param>
        private void ReceiveViewMessage(String requestedUpdateViewModel)
        {
            switch (requestedUpdateViewModel)
            {
                case "ViewHuntViewModel":
                    {
                        CurrentViewModel = MainViewModel.viewHuntViewModel;
                        break;
                    }
                case "SearchHuntViewModel":
                    {
                        CurrentViewModel = MainViewModel.searchHuntViewModel;
                        break;
                    }
                case "PrintViewModel":
                    {
                        CurrentViewModel = MainViewModel.printViewModel;
                        break;
                    }
                case "CreateHuntViewModel":
                    {
                        CurrentViewModel = MainViewModel.createHuntViewModel;
                        break;
                    }
                case "LoginViewModel":
                    {
                        CurrentViewModel = MainViewModel.loginViewModel;
                        break;
                    }
                case "RegisterViewModel":
                    {
                        CurrentViewModel = MainViewModel.registerViewModel;
                        break;
                    }
                case "LeaderboardViewModel":
                    {
                        CurrentViewModel = MainViewModel.leaderboardViewModel;
                        break;
                    }
                case "RetrieveEmailViewModel":
                    {
                        CurrentViewModel = MainViewModel.retrieveEmailViewModel;
                        break;
                    }
                case "ResetPasswordViewModel":
                    {
                        CurrentViewModel = MainViewModel.resetPasswordViewModel;
                        break;
                    }
                case "ResetCompanyPasswordViewModel":
                    {
                        CurrentViewModel = MainViewModel.resetCompanyPasswordViewModel;
                        break;
                    }
            }

        #endregion
        #endregion

        }
    }
}