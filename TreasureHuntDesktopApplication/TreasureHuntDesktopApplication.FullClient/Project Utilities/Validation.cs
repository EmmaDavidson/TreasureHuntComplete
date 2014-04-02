using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using System.Windows.Controls;

//----------------------------------------------------------
//<copyright>
//</copyright>
//----------------------------------------------------------

namespace TreasureHuntDesktopApplication.FullClient.Project_Utilities
{
    /// <Summary> The purpose of this class is to provide validation checks common to different ViewModels. </Summary>

    public static class Validation
    {
        /// <summary>
        /// Method that determines whether a String is null or has whitespace. 
        /// </summary>
        /// <param name="stringToCheck"></param>
        /// <returns></returns>
        public static bool IsNullOrWhiteSpace(String stringToCheck)
        {
            if (String.IsNullOrWhiteSpace(stringToCheck))
            {
                return true;
            }
            
            return false;
        }

        /// <summary>
        /// Method that determines whether a String is null or empty. 
        /// </summary>
        /// <param name="stringToCheck"></param>
        /// <returns></returns>
        public static bool IsNullOrEmpty(String stringToCheck)
        {
            if (String.IsNullOrEmpty(stringToCheck) || String.IsNullOrWhiteSpace(stringToCheck))
            {
                return true;
            }
            return false;
        }

        /// <summary>
        /// Method that determines whether a String is valid in relation to the opposing lengths given. 
        /// </summary>
        /// <param name="stringToCheck"></param>
        /// <param name="maxLength"></param>
        /// <param name="minLength"></param>
        /// <returns></returns>
        public static bool IsValidLength(String stringToCheck, int maxLength, int minLength)
        {
            if(stringToCheck != null)
            {
                if (stringToCheck.Length <= maxLength && stringToCheck.Length >= minLength)
                {
                    return true;
                }
            }
            return false;
        }

        /// <summary>
        ///  Method to determine whether a String contains valid characters. 
        /// </summary>
        /// <param name="stringToCheck"></param>
        /// <returns></returns>
        public static bool IsValidCharacters(String stringToCheck)
        {
            if (stringToCheck != null)
            {
                if (Regex.IsMatch(stringToCheck, @"^[a-zA-Z ]+$"))
                {
                    return true;
                }
            }
            return false;
        }

        public static bool IsValidPasswordCharacters(String stringToCheck)
        {
            if (stringToCheck != null)
            {
                if (Regex.IsMatch(stringToCheck, @"^[a-zA-Z 0-9]+$"))
                {
                    return true;
                }
            }
            return false;
        }

        /// <summary>
        /// Method that determines whether a String in the correct email format. 
        /// </summary>
        /// <param name="emailToCheck"></param>
        /// <returns></returns>
        public static bool IsValidEmail(String emailToCheck)
        {
            if (emailToCheck != null)
            {
                //-http://stackoverflow.com/questions/16167983/best-regular-expression-for-email-validation-in-c-sharp
                if (Regex.IsMatch(emailToCheck, @"^([\w\.\-]+)@([\w\-]+)((\.(\w){2,3})+)$"))
                {
                    return true;
                }
            }
            return false;
        }
    }
}
