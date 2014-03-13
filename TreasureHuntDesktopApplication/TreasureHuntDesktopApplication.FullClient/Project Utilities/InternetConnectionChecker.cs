using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TreasureHuntDesktopApplication.FullClient.Project_Utilities
{
    public class InternetConnectionChecker
    {
        //Make this into a singleton?
        //This needs to be checked at home
        //-http://etaktix.blogspot.co.uk/2013/01/check-if-internet-connection-is.html
        public static bool IsInternetConnected()
        {
            if (!System.Net.NetworkInformation.NetworkInterface.GetIsNetworkAvailable())
            {
                return false;
            }

            return true;
        }

        public static String ShowConnectionErrorMessage()
        {
            return "You are not connected to the internet. Please try again when you have a working connection.";
        }
    }
}
