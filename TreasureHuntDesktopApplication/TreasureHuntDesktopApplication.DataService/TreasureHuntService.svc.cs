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


/// <Summary> The purpose of this Web Service is to host methods that create, read, update or delete (CRUD) entries 
/// in the database from the WPF application. 

namespace TreasureHuntDesktopApplication.DataService
{
    public class TreasureHuntService : ITreasureHuntService
    {
        #region Methods associated with: hunts and huntparticipants
        /// <summary>
        /// Method that returns all treasure hunts that exist in the 'hunts' table. 
        /// </summary>
        /// <returns></returns>
        public IEnumerable<hunt> GetTreasureHunts()
        {
            using (var context = new TreasureHuntEntities())
            {
                var returnedHunts = context.hunts.ToList();
                returnedHunts.ForEach(e => context.ObjectStateManager.ChangeObjectState(e, System.Data.EntityState.Detached));
                return returnedHunts;

            }
        }

        /// <summary>
        ///  Method that returns a list of treasure hunts for a given user. 
        /// </summary>
        /// <param name="user"></param>
        /// <returns></returns>
        public IEnumerable<hunt> GetTreasureHuntsForParticularUser(user user)
        {
            using (var context = new TreasureHuntEntities())
            {
                if (user == null) return null;
                //Grab of all HuntIds associated with the given user.
                var returnedHuntIds = context.userhunts.Where(c => c.UserId == user.UserId).Select(s => s.HuntId).ToList();
                returnedHuntIds.ForEach(e => context.ObjectStateManager.ChangeObjectState(e, System.Data.EntityState.Detached));

                List<hunt> returnedListOfHunts = new List<hunt>();

                //For each of these ids
                foreach (var huntId in returnedHuntIds)
                {
                    //Add to a list the hunt associated with this id.
                    var hunt = context.hunts.Where(c => c.HuntId == huntId).Single();
                    context.ObjectStateManager.ChangeObjectState(hunt, System.Data.EntityState.Detached);
                    returnedListOfHunts.Add(hunt);
                }

                //Return this list.
                return returnedListOfHunts;
            }
        }

        /// <summary>
        /// Method that saves new hunt data into the 'hunts' table. 
        /// </summary>
        /// <param name="newHunt"></param>
        /// <returns></returns>
        public long SaveNewHunt(hunt newHunt)
        {
            using (var context = new TreasureHuntEntities())
            {
                context.hunts.AddObject(newHunt);
                context.SaveChanges();
                context.ObjectStateManager.ChangeObjectState(newHunt, System.Data.EntityState.Added);
                return newHunt.HuntId;
            }
        }

        //-http://stackoverflow.com/questions/15576282/sorting-a-c-sharp-list-by-multiple-columns
        //-http://stackoverflow.com/questions/18525253/wpf-listt-sort
        /// <summary>
        /// Method that returns all the huntparticipant data for a given treasure hunt. 
        /// </summary>
        /// <param name="currentTreasureHunt"></param>
        /// <returns></returns>
        public List<huntparticipant> GetHuntParticipants(hunt currentTreasureHunt)
        {
            using (var context = new TreasureHuntEntities())
            {
                if (currentTreasureHunt == null) return null;
                var returnedHuntParticipants = context.huntparticipants.Where(c => c.HuntId == currentTreasureHunt.HuntId).OrderByDescending(s => s.Tally).ThenBy(s => s.ElapsedTime).ToList();
                returnedHuntParticipants.ForEach(e => context.ObjectStateManager.ChangeObjectState(e, System.Data.EntityState.Detached));
                return returnedHuntParticipants;
            }
        }

        /// <summary>
        /// Method that returns the name of the participant associated with a given id. 
        /// </summary>
        /// <param name="userId"></param>
        /// <returns></returns>
        public user GetParticipant(long userId)
        {
            using (var context = new TreasureHuntEntities())
            {
                var returnedHuntParticipant = context.users.Where(c => c.UserId == userId).Single();
                context.ObjectStateManager.ChangeObjectState(returnedHuntParticipant, System.Data.EntityState.Detached);
                return returnedHuntParticipant;
            }
        }
        #endregion 
        
        #region Methods associated with: huntquestions and questions
        /// <summary>
        /// Method that returns a list of questions for a given hunt. 
        /// </summary>
        /// <param name="hunt"></param>
        /// <returns></returns>
        public IEnumerable<question> GetHuntQuestions(hunt hunt)
        { 
            using (var context = new TreasureHuntEntities())
            {
                if (hunt == null) return null;
                //Grab all of the question ids associated with a given hunt.
                var returnedquestionIds = context.huntquestions.Where(c => c.HuntId == hunt.HuntId).Select(s => s.QuestionId).ToList();
                returnedquestionIds.ForEach(e => context.ObjectStateManager.ChangeObjectState(e, System.Data.EntityState.Detached));

                List<question> returnedListOfQuestions = new List<question>();

                //For each one of these ids
                foreach (var questionId in returnedquestionIds)
                {
                    //Add to the list the question associated with this id.
                    var question = context.questions.Where(c => c.QuestionId == questionId).Single();
                    context.ObjectStateManager.ChangeObjectState(question, System.Data.EntityState.Detached);
                    returnedListOfQuestions.Add(question);
                }

                return returnedListOfQuestions;
            }
        }

        /// <summary>
        /// Method that returns a question for a given question id.
        /// </summary>
        /// <param name="questionId"></param>
        /// <returns></returns>
        public question GetQuestion(long questionId)
        {
            using (var context = new TreasureHuntEntities())
            {
                var returnedQuestions = context.questions.Where(c => c.QuestionId == questionId).Single();
                context.ObjectStateManager.ChangeObjectState(returnedQuestions, System.Data.EntityState.Detached);
                return returnedQuestions;
            }
        }

        /// <summary>
        /// Method that saves a given question into the 'questions' table. 
        /// </summary>
        /// <param name="newQuestion"></param>
        /// <returns></returns>
        public long SaveQuestion(question newQuestion)
        {
            using (var context = new TreasureHuntEntities())
            {
                context.questions.AddObject(newQuestion);
                context.SaveChanges();
                context.ObjectStateManager.ChangeObjectState(newQuestion, System.Data.EntityState.Added);
                return newQuestion.QuestionId;
            }
        }

        /// <summary>
        /// Method that saves new huntQuestion data into the 'huntquestions' table. 
        /// </summary>
        /// <param name="huntQuestion"></param>
        public void SaveNewHuntQuestion(huntquestion huntQuestion)
        {
            using (var context = new TreasureHuntEntities())
            {
                context.huntquestions.AddObject(huntQuestion);
                context.SaveChanges();
                context.ObjectStateManager.ChangeObjectState(huntQuestion, System.Data.EntityState.Added);
            }
        }
        #endregion 
              
        #region Methods associated with: users, userroles and userhunts

        /// <summary>
        /// Method that returns a hunt if one exists for a given name for a given user. 
        /// </summary>
        /// <param name="name"></param>
        /// <param name="userId"></param>
        /// <returns></returns>
        public hunt GetHuntBasedOnName(String name, long userId)
        {
            using (var context = new TreasureHuntEntities())
            {
                //Get all of this usershunts ids for the given user.
                var returnedUserHunts = context.userhunts.Where(c => c.UserId == userId).ToList();
                context.ObjectStateManager.ChangeObjectState(returnedUserHunts, System.Data.EntityState.Detached);

                //For each of these hunt ids, 
                foreach (var userHunts in returnedUserHunts)
                {
                    //Grab that hunt,
                    var hunt = context.hunts.Where(c => c.HuntId == userHunts.HuntId).Single();
                    context.ObjectStateManager.ChangeObjectState(hunt, System.Data.EntityState.Detached);

                    //If that hunt name is the same as expected, return that hunt.
                    if (hunt.HuntName == name)
                    {
                        return hunt;
                    }
                }
                return null;
            }
        }

        /// <summary>
        /// Method that returns all existing users that are found in the 'users' table.
        /// </summary>
        /// <returns></returns>
        public List<user> GetExistingUsers()
        {
            using (var context = new TreasureHuntEntities())
            {
                var returnedUsers = context.users.ToList();
                returnedUsers.ForEach(e => context.ObjectStateManager.ChangeObjectState(e, System.Data.EntityState.Detached));
                return returnedUsers;
            }
        }

        /// <summary>
        /// Method that saves a new user into the 'users' table. 
        /// </summary>
        /// <param name="newUser"></param>
        /// <returns></returns>
        public long SaveUser(user newUser)
        {
            using (var context = new TreasureHuntEntities())
            {
                context.users.AddObject(newUser);
                context.SaveChanges();
                context.ObjectStateManager.ChangeObjectState(newUser, System.Data.EntityState.Added);
                return newUser.UserId;
            }
        }

        /// <summary>
        /// Method that saves new UserRole data into the 'userroles' table. 
        /// </summary>
        /// <param name="newUserRole"></param>
        public void SaveUserRole(userrole newUserRole)
        {
            using (var context = new TreasureHuntEntities())
            {
                context.userroles.AddObject(newUserRole);
                context.SaveChanges();
                context.ObjectStateManager.ChangeObjectState(newUserRole, System.Data.EntityState.Added);
            }
        }

        /// <summary>
        ///  Method that returns a user associated with the given email address.
        /// </summary>
        /// <param name="emailAddress"></param>
        /// <returns></returns>
        public user GetUser(string emailAddress)
        {
            using (var context = new TreasureHuntEntities())
            {
                if (context.users.Any(c => c.Email == emailAddress))
                {
                    var returnedUser = context.users.Where(c => c.Email == emailAddress).Single();
                    context.ObjectStateManager.ChangeObjectState(returnedUser, System.Data.EntityState.Detached);
                    return returnedUser;
                }

                return null;
            }
        }

        /// <summary>
        /// Method that returns the userrole data associated with the given user. 
        /// </summary>
        /// <param name="user"></param>
        /// <returns></returns>
        public userrole GetUserRole(user user)
        {
            using (var context = new TreasureHuntEntities())
            {
                var returnedUserRole = context.userroles.Where(c => c.UserId == user.UserId).Single();
                context.ObjectStateManager.ChangeObjectState(returnedUserRole, System.Data.EntityState.Detached);
                return returnedUserRole;
            }
        }

        /// <summary>
        /// Method that saves userhunt data in the 'userhunts' table.
        /// </summary>
        /// <param name="userHunt"></param>
        public void SaveUserHunt(userhunt userHunt)
        {
            using (var context = new TreasureHuntEntities())
            {
                context.userhunts.AddObject(userHunt);
                context.SaveChanges();
                context.ObjectStateManager.ChangeObjectState(userHunt, System.Data.EntityState.Added);
            }
        }

        /// <summary>
        /// Method that updates the login password associated with a given user.
        /// </summary>
        /// <param name="currentUser"></param>
        /// <param name="newPassword"></param>
        public void updateUserPassword(user currentUser, String newPassword)
        {
            using (var context = new TreasureHuntEntities())
            {
                var userToChange = context.users.Where(c => c.UserId == currentUser.UserId).Single();
                userToChange.Password = newPassword;
                context.SaveChanges();
                context.ObjectStateManager.ChangeObjectState(userToChange, System.Data.EntityState.Modified);
            }
        }

        #endregion  

        #region Methods associated with: usersecurityquestions and securityquestions
        /// <summary>
        /// Method that saves new usersecurityquestion data into the 'usersecurityquestions' table. 
        /// </summary>
        /// <param name="userSecurityQuestion"></param>
        public void SaveUserSecurityQuestion(usersecurityquestion userSecurityQuestion)
        {
            using (var context = new TreasureHuntEntities())
            {
                context.usersecurityquestions.AddObject(userSecurityQuestion);
                context.SaveChanges();
                context.ObjectStateManager.ChangeObjectState(userSecurityQuestion, System.Data.EntityState.Added);
            }
        }

        /// <summary>
        /// Method that returns all of the security questions available in the 'securityquestions' table. 
        /// </summary>
        /// <returns></returns>
        public IEnumerable<securityquestion> getListOfSecurityQuestions()
        {
            using (var context = new TreasureHuntEntities())
            {
                var returnedQuestions = context.securityquestions.ToList();
                returnedQuestions.ForEach(e => context.ObjectStateManager.ChangeObjectState(e, System.Data.EntityState.Detached));
                return returnedQuestions;
            }
        }

        /// <summary>
        /// Method that returns all of the securityquestion data associated with a given user.
        /// </summary>
        /// <param name="user"></param>
        /// <returns></returns>
        public securityquestion getUserSecurityQuestion(user user)
        {
            using (var context = new TreasureHuntEntities())
            {
                if (user == null) return null;

                //Grab the user's securityquestionId
                var returnedUserSecurityQuestionId = context.usersecurityquestions.Where(c => c.UserId == user.UserId).Select(s => s.SecurityQuestionId).Single();
                context.ObjectStateManager.ChangeObjectState(returnedUserSecurityQuestionId, System.Data.EntityState.Detached);

                //Grab the security question associated with this id. 
                var returnedQuestion = context.securityquestions.Where(c => c.SecurityQuestionId == returnedUserSecurityQuestionId).Single();
                context.ObjectStateManager.ChangeObjectState(returnedQuestion, System.Data.EntityState.Detached);

                return returnedQuestion;
            }
        }

        /// <summary>
        ///  Method that returns the securityquestion answer associated with a given user.
        /// </summary>
        /// <param name="currentUser"></param>
        /// <returns></returns>
        public usersecurityquestion getUserSecurityAnswer(user currentUser)
        {
            using (var context = new TreasureHuntEntities())
                {
                    if (currentUser == null) return null;

                    var returnedUserSecurityQuestionDetails = context.usersecurityquestions.Where(c => c.UserId == currentUser.UserId).Single();
                    context.ObjectStateManager.ChangeObjectState(returnedUserSecurityQuestionDetails, System.Data.EntityState.Detached);

                    return returnedUserSecurityQuestionDetails;
                }
        }

        #endregion 

        #region Methods associated with: companydetails

        /// <summary>
        /// Method that updates the company password associated with a given user.
        /// </summary>
        /// <param name="currentUser"></param>
        /// <param name="newPassword"></param>
        public void updateCompanyPassword(user currentUser, String newPassword)
        {
            using (var context = new TreasureHuntEntities())
            {
                var companyToChange = context.companydetails.Where(c => c.UserId == currentUser.UserId).Single();
                companyToChange.CompanyPassword = newPassword;
                context.SaveChanges();
                context.ObjectStateManager.ChangeObjectState(companyToChange, System.Data.EntityState.Modified);
            }
        }

        /// <summary>
        /// Method that saves company data into the 'companydetails' table. 
        /// </summary>
        /// <param name="companyDetails"></param>
        public void saveCompany(companydetail companyDetails)
        {
            using(var context = new TreasureHuntEntities())
            {
                context.companydetails.AddObject(companyDetails);
                context.SaveChanges();
                context.ObjectStateManager.ChangeObjectState(companyDetails, System.Data.EntityState.Added);
            }
        }

        /// <summary>
        ///  Method that returns all of the companys found in the 'companydetails' table.
        /// </summary>
        /// <returns></returns>
        public List<companydetail> getExistingCompanies()
        {
            using (var context = new TreasureHuntEntities())
            {
                var returnedCompanies = context.companydetails.ToList();
                returnedCompanies.ForEach(e => context.ObjectStateManager.ChangeObjectState(e, System.Data.EntityState.Detached));
                return returnedCompanies;
            }
        }
        #endregion 
    }
}
