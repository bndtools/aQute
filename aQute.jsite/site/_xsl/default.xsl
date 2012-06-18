<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">
	<xsl:output method="html" doctype-system="about:legacy-compat"
		encoding="UTF-8" indent="no" />


	<xsl:variable name="site" select="document('site')/site" />

	<!-- Content:template -->

	<xsl:template match="/">
		<xsl:param name="path" />
		<html lang="en">
			<head>
				<meta charset="utf-8" />
				<meta name="description" content="Just another Package Manager for Java" />
				<meta name="author" content="aQute SARL" />

				<link href="css/bootstrap.css" rel="stylesheet" />

				<style type="text/css">
					/* Override some defaults */
					html, body {
					background-color: #eee;
					}
					body {
					padding-top: 60px; /* 40px to make
					the container go all the
					way to the bottom
					of the topbar */
					}
					.container > footer p {
					text-align: center; /* center align it with
					the container */
					}
					/* The white background content
					wrapper */
					.container > .content {
					background-color: #fff;
					padding:
					20px;
					margin:
					0 -20px; /* negative
					indent the amount of the padding
					to maintain
					the
					grid system */
					-webkit-border-radius: 6px 6px 6px 6px;
					-moz-border-radius: 6px 6px 6px 6px;
					border-radius: 6px 6px 6px 6px;
					-webkit-box-shadow: 0 3px 5px
					rgba(0,0,0,.15);
					-moz-box-shadow: 0
					1px
					2px rgba(0,0,0,.15);
					box-shadow: 0 1px 2px rgba(0,0,0,.15);
					}

					/*
					Page
					header tweaks */
					.page-header {
					background-color: #f5f5f5;
					padding:
					20px 20px 10px;
					margin: -20px -20px 20px;
					}

					/* Give a
					quick and
					non-cross-browser
					friendly divider */
					.content .span4 {
					margin-left:
					0;
					padding-left:
					19px;
					border-left: 1px solid #eee;
					}

					.topbar .btn {
					border: 0;
					}

					.active {
					text-decoration:none;
					}
				</style>


				<title>
					<xsl:value-of select="$site/@prefix" />
					-
					<xsl:value-of select="content/h2[1]" />
				</title>


				<link rel="shortcut icon" href="images/favicon.ico" />
				<link rel="apple-touch-icon" href="images/apple-touch-icon.png" />
				<link rel="apple-touch-icon" sizes="72x72"
					href="images/apple-touch-icon-72x72.png" />
				<link rel="apple-touch-icon" sizes="114x114"
					href="images/apple-touch-icon-114x114.png" />
			</head>

			<body>
				<div class="topbar">
					<div class="fill">
						<div class="container span16">
							<a class="brand" href="index.html">
								<img height="32px" src="img/jpm-white.png" />
							</a>
							<ul class="nav">
								<li>
									<a href="index.html">Home</a>
								</li>
								<li>
									<a href="search.html">Search</a>
								</li>
								<li>
									<a href="about.html">About</a>
								</li>
								<li>
									<a href="contact.html">Contact</a>
								</li>
							</ul>
							<form action="" class="pull-right">
								<input class="input-small" type="text" placeholder="Search" />
								<button class="btn" type="submit">Go</button>
							</form>
						</div>
					</div>
				</div>
				<div class="container">

					<div class="content">
						<div class="row">
							<img src="img/home.png" class="span16"/>
						</div>
						<br/>
						<div class="row">
							<div class="span12">
								<xsl:copy-of select="content/*" />
							</div>
							<div class="span4">
								<h3>Latest Uploads</h3>
							</div>
						</div>
					</div>

					<div style="margin-top:40px;" class="content shadow">
						<div class="row">
							<div class="span4">
								col 1
							</div>
							<div class="span4">
								col 2
							</div>
							<div class="span4">
								col 3
							</div>
							<div class="span4">
								col 4
							</div>
						</div>
					</div>

					<footer>
						<p>(c)
							aQute SARL 2011
						</p>
					</footer>

				</div> <!-- /container -->
			</body>
		</html>
	</xsl:template>

</xsl:stylesheet>