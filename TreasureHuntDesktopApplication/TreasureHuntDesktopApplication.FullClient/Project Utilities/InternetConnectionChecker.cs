using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

//----------------------------------------------------------
//<copyright>
//</copyright>
//----------------------------------------------------------

namespace TreasureHuntDesktopApplication.FullClient.Project_Utilities
{
     /// <Summary> The purpose of this singleton is to be a helper class with relation to the WPF application and the internet connection
     /// of a user's machine. </Summary>
    public class InternetConnectionChecker
    {
        //-http://etaktix.blogspot.co.uk/2013/01/check-if-internet-connection-is.html

        private static InternetConnectionChecker connectionChecker;

        /// <Summary> Private constructor </Summary>
        private InternetConnectionChecker() { }

        /// <summary>
        ///  Method that returns an instance of the checker 
        /// </summary>
        /// <returns></returns>
        public static InternetConnectionChecker GetInstance()
        {
            if (connectionChecker == null)
            {
                connectionChecker = new InternetConnectionChecker();
            }

            return connectionChecker;
        }

        /// <summary>
        /// Method that determines whether or not the device is connected to the Internet. 
        /// </summary>
        /// <returns></returns>
        public bool IsInternetConnected()
        {
            if (!System.Net.NetworkInformation.NetworkInterface.GetIsNetworkAvailable())
            {
                return false;
            }

            return true;
        }

        /// <summary>
        /// Method that returns an error message string if the Internet is not connected. 
        /// </summary>
        /// <returns></returns>
        public String ShowConnectionErrorMessage()
        {
            return "You are not connected to the internet. Please try again when you have a working connection.";
        }
    }
}
