using System.Windows;
using TreasureHuntDesktopApplication.FullClient.ViewModel;

namespace TreasureHuntDesktopApplication.FullClient.Views
{
    /// <summary>
    /// This application's main window.
    /// </summary>
    public partial class MainWindow : Window
    {
        private MainViewModel viewModel = new MainViewModel();

        public MainWindow()
        {
            InitializeComponent();
            this.DataContext = viewModel;
           // Closing += (s, e) => ViewModelLocator.Cleanup();
        }

    
    }
}