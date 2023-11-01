# Security Policy

## Supported Versions

OPKJE is released with Opticia game packed. Unless specified additionally, only the latest version is supported.

## Reporting a Vulnerability

A **PUBLIC** issue or pull request can be raised under the following cases:

- Bugs or performance issues whose impacts are **LOCAL**.
  
  They usually won't bring extra security concerns. By making them public, the issue can be resolved **much faster**.
  
  Examples:
  
  - "Note Textures Are Missing for Several Charts"
  
  - "Possible Memory Leaks When Retreiving Content from URL"

- Incorrect behavior of certain JavaScript APIs without security-related cases.
  
  When an API does not work, charts using them will likely to malfunction. By making them public, we can get more people in touch with the issue.
  
  Examples:
  
  - "Incorrect Version Number Returned by Version API"
  
  - "Empty List When Querying for Downloaded Charts"

Under such cases, simply raise your issue in the issue page. If you are proposing changes, a pull request is also highly welcome.

---

A **PRIVATE** mail or message should be sent to us under the following cases. **DO NOT** post them publicly:

- Vulnerabilities with publicly documented JavaScript APIs.
  
  These APIs are used by charts and Opticia and are not protected. If they have vulnerabilities undiscovered, malicious chart code can utilitize them to hack the client system.
  
  Examples:
  
  - "Finder Does Not Verify Path When Reading a File"
  
  - "Possible RCE When Importing Library"

- Vulnerabilities whose impacts are **SEVERE** or **REMOTE**.
  
  Though not directly exported, malicious code might still hack in using methods we haven't considered of.
  
  Examples:
  
  - "System Path Got Erased When Deleting Chart from External Drive"
  
  - "Buffer Overflow Cause Tokens to Be Uploaded to 3rd-parties"

- Considerations, issues with private information related, or whose type which can't be determined.

When a private report is necessary, please send mail to [skjsjhb@outlook.com](mailto:skjsjhb@outlook.com). However, this is one of the personal mail addresses of our developers. Its inbox is not checked frequently. We'll drop in a new dedicated official E-mail address here soon.
