Sentinel API - Signing requests
===============================

:Author: marco@alertavert.com (Marco Massenzio)
:Revision: 1.0
:Created: 2014-09-07
:Updated: 2014-09-07


Goals
-----

Every API request must be authenticated by signing it using the user's ``api_key``; this
does not, by itself, secures the request from *man-in-the-middle* attacks, but it does
prevent forging bogus requests.

The request's body and (some of) its headers will be included in the hash.

Protocol
--------

When a user sends an API request that needs to be authenticated (not all,
but most requests do) an ``Authorization`` [#]_ header must be present, which includes the following
fields::

	Authorization: username="joe@acme.com";qop="auth-int";hash_func=SHA-256;hash=...hex...

we do not use "nonces"; however, the ``Date`` header **MUST** be present::

	Date: Tue, 13 Nov 2014 08:12:31 UTC

and it will be included in the hash; requests whose timestamp are older than a given
system-wide threshold (TBD) will be rejected; only UTC timestamps are accepted (transitional).

	``qop``
		Indicates that both HTTP method, URL, headers and entity body are included in the hash

	``hash_func``
		The name of the hash function used; currently only ``SHA-256`` is supported.

	``hash``
		The hex-encoded result of hashing the following fields in this exact order, no
		space separators, exactly as they are present in the request::

			api_key (hex-encoded, as returned during the login request)
			HTTP method (GET, POST, etc.)
			HTTP URL (exactly as represented in the request)
			HTTP Headers: Date, Content-Length, Content-Type
			HTTP Body, if present

Response
--------

If the authentication fails (that is, the server-side computed SHA-256 hash of the fields
described above differs from the value sent by the client) an error responses is sent back::

	401 Unauthorized

	{
		"error": "The request could not be authenticated for user joe@acme.com"
	}

For security reasons, no additional detail is provided.

Successful authentication results in the requests to be further processed - howerver, depending
on the **authorization** level of the user's request, this may still fail with a ``401`` error code
(eg, if the client is attempting to access a service that the user is not authorized for).


Notes
-----

.. [#] Note that this is, strictly speaking, an **authentication** field, not
	   an *authorization* one; alas, HTTP headers have been codified as such.
	   
