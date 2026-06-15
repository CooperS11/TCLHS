const Auth = (() => {
  const USER_KEY  = 'tclhsUserId';
  const ROLE_KEY  = 'tclhsUserRole';
  const TUTOR_KEY = 'tclhsTutorId';

  function getUserId() {
    return localStorage.getItem(USER_KEY);
  }

  function getRole() {
    return localStorage.getItem(ROLE_KEY) || 'student';
  }

  function setRole(role) {
    localStorage.setItem(ROLE_KEY, role);
  }

  function getTutorId() {
    return localStorage.getItem(TUTOR_KEY);
  }

  function setTutorId(id) {
    if (id) localStorage.setItem(TUTOR_KEY, id);
    else localStorage.removeItem(TUTOR_KEY);
  }

  function hasTutorProfile() {
    return !!getTutorId();
  }

  function getEffectiveRole() {
    if (hasTutorProfile()) return 'both';
    const role = getRole();
    return (role === 'tutor') ? 'student' : role;
  }

  function homePageForRole(role) {
    if (role === 'tutor') return 'tutor-home.html';
    if (role === 'both') return 'student-home.html';
    return 'student-home.html';
  }

  // Redirect to login if not signed in. Returns true if signed in.
  function requireLoggedIn() {
    if (!getUserId()) {
      window.location.href = 'login.html';
      return false;
    }
    return true;
  }

  // On login/register pages: redirect to the user's home if already signed in.
  function requireGuest() {
    if (getUserId()) {
      window.location.href = homePageForRole(getEffectiveRole());
      return false;
    }
    return true;
  }

  // allowedRoles: e.g. ['student', 'both'] or ['tutor', 'both']
  // Redirects to the user's correct home if their role isn't in the list.
  function requireRole(allowedRoles) {
    if (!requireLoggedIn()) return false;
    const role = getEffectiveRole();
    if (!allowedRoles.includes(role)) {
      window.location.href = homePageForRole(role);
      return false;
    }
    return true;
  }

  function signOut() {
    localStorage.removeItem(USER_KEY);
    localStorage.removeItem(ROLE_KEY);
    localStorage.removeItem(TUTOR_KEY);
    window.location.href = 'login.html';
  }

  return { getUserId, getRole, getEffectiveRole, setRole, getTutorId, setTutorId, hasTutorProfile, homePageForRole, requireLoggedIn, requireGuest, requireRole, signOut };
})();
