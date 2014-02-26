using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight.Messaging;
using System;
using System.ComponentModel;
using System.Windows.Input;
using TreasureHuntDesktopApplication.FullClient.Messages;
using TreasureHuntDesktopApplication.FullClient.Model;
using TreasureHuntDesktopApplication.FullClient.TreasureHuntService;

namespace TreasureHuntDesktopApplication.FullClient.ViewModel
{
    public class MainViewModel : ViewModelBase
    {
        //-http://www.codeproject.com/Articles/72724/Beginning-a-WPF-MVVM-application-Navigating-betwee

        private ViewModelBase currentViewModel;
        private static readonly TreasureHuntServiceClient serviceClient = new TreasureHuntServiceClient();

        readonly static CreateHuntViewModel createHuntViewModel = new CreateHuntViewModel(serviceClient);
        readonly static ViewHuntViewModel viewHuntViewModel = new ViewHuntViewModel(serviceClient);
        readonly static SearchHuntViewModel searchHuntViewModel = new SearchHuntViewModel(serviceClient);
        readonly static PrintViewModel printViewModel = new PrintViewModel(serviceClient);
        readonly static LoginViewModel loginViewModel = new LoginViewModel(serviceClient);
        readonly static RegisterViewModel registerViewModel = new RegisterViewModel(serviceClient);
        readonly static LeaderboardViewModel leaderboardViewModel = new LeaderboardViewModel(serviceClient);


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

        public MainViewModel()
        {
            CurrentViewModel = MainViewModel.loginViewModel;

            Messenger.Default.Register<UpdateViewMessage>
                (
                
                this,
                (action) => ReceiveViewMessage(action.UpdateViewTo)
                
                );
        }

        private void ReceiveViewMessage(String requestedUpdateViewModel)
        {
            if (requestedUpdateViewModel == "ViewHuntViewModel")
            {
                CurrentViewModel = MainViewModel.viewHuntViewModel;
            }
            else if (requestedUpdateViewModel == "SearchHuntViewModel")
            {
                CurrentViewModel = MainViewModel.searchHuntViewModel;
            }
            else if (requestedUpdateViewModel == "PrintViewModel")
            {
                CurrentViewModel = MainViewModel.printViewModel;
            }
            else if (requestedUpdateViewModel == "CreateHuntViewModel")
            {
                CurrentViewModel = MainViewModel.createHuntViewModel;
            }
            else if (requestedUpdateViewModel == "LoginViewModel")
            {
                CurrentViewModel = MainViewModel.loginViewModel;
            }
            else if (requestedUpdateViewModel == "RegisterViewModel")
            {
                CurrentViewModel = MainViewModel.registerViewModel;
            }
            else if (requestedUpdateViewModel == "LeaderboardViewModel")
            {
                CurrentViewModel = MainViewModel.leaderboardViewModel;
            }

        }
    }
}