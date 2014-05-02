using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.ServiceModel;
using System.Text;
using TreasureHuntDesktopApplication.Data;

//----------------------------------------------------------
//<copyright>
//Emma Davidson - Treasure Hunt 2013-3014 Final Year Project
//</copyright>
//----------------------------------------------------------

 /// <Summary>The purpose of this interface is to permit TreasureHuntService access to its methods i.e. it is a 
 /// contract with the service. Also implemented for ease of testing TreasureHuntService. </Summary>

namespace TreasureHuntDesktopApplication.DataService
{
    [ServiceContract]
    public interface ITreasureHuntService
    {
        [OperationContract]
        long SaveQuestion(question newQuestion);

        [OperationContract]
        void SaveNewHuntQuestion(huntquestion huntQuestion);

        [OperationContract]
        long SaveNewHunt(hunt newHunt);

        [OperationContract]
        IEnumerable<question> GetHuntQuestions(hunt hunt);

        [OperationContract]
        IEnumerable<hunt> GetTreasureHunts();

        [OperationContract]
        question GetQuestion(long questionId);

        [OperationContract]
        hunt GetHuntBasedOnName(String name, long userId);

        [OperationContract]
        List<user> GetExistingUsers();

        [OperationContract]
        long SaveUser(user newUser);

        [OperationContract]
        void SaveUserRole(userrole newUserRole);

        [OperationContract]
        user GetUser(string emailAddress);

        [OperationContract]
        userrole GetUserRole(user user);

        [OperationContract]
        void SaveUserHunt(userhunt userHunt);

        [OperationContract]
        IEnumerable<hunt> GetTreasureHuntsForParticularUser(user user);

        [OperationContract]
        List<huntparticipant> GetHuntParticipants(hunt currentTreasureHunt);

        [OperationContract]
        user GetParticipant(long participant);

        [OperationContract]
        void SaveUserSecurityQuestion(usersecurityquestion userSecurityQuestion);

        [OperationContract]
        IEnumerable<securityquestion> getListOfSecurityQuestions();

        [OperationContract]
        securityquestion getUserSecurityQuestion(user user);

        [OperationContract]
        usersecurityquestion getUserSecurityAnswer(user currentUser);

        [OperationContract]
        void updateUserPassword(user currentUser, String newPassword);

        [OperationContract]
        void updateCompanyPassword(user currentUser, String newPassword);

        [OperationContract]
        List<companydetail> getExistingCompanies();

        [OperationContract]
        void saveCompany(companydetail companyDetails);
    }
}
