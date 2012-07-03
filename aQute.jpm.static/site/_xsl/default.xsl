<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">
	<xsl:output method="html" doctype-system="about:legacy-compat"
		encoding="UTF-8" indent="no" />


	<xsl:variable name="site" select="document('site')/site" />

	<xsl:template match="/">
		<xsl:param name="path" />
		<html lang="en">
			<head>
				<meta charset="utf-8" />
				<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
				<meta name="viewport" content="width=device-width, initial-scale=1" />
				<link href="css/style.css" rel="stylesheet" />
				<title>
					<xsl:value-of select="$site/@prefix" />
					-
					<xsl:value-of select="//h1[1]" />
				</title>
			</head>

			<body>
				<!-- Facebook -->
				<div id="fb-root"></div>
				<script>(function(d, s, id) {
				  var js, fjs = d.getElementsByTagName(s)[0];
				  if (d.getElementById(id)) return;
				  js = d.createElement(s); js.id = id;
				  js.src = "//connect.facebook.net/en_US/all.js#xfbml=1";
				  fjs.parentNode.insertBefore(js, fjs);
				}(document, 'script', 'facebook-jssdk'));
				</script>

				<div id="nav">
					<ul>
						<li>
							<!-- Reserved for logo -->
							<a href="index.html">&#160;</a>
						</li>
						<li>
							<a href="jpm.html#/">Browse</a>
						</li>
						<li>
							<a href="install.html">Install</a>
						</li>
						<li>
							<a href="about.html">About</a>
						</li>
						<li>
							<!-- Last child is search box -->
							Search
							<input type="text" />
						</li>
					</ul>
				</div>
				<div id="navreserve" />
				<div id="page">
					<div id="external">
						<xsl:copy-of select="content/*" />
					</div>
					<div id="side">
						<h2>Sidebars</h2>

					</div>
				</div>

				<div id="footer">
					<div id="aQute">
						<h2>aQute</h2>
						<ul>
							<li>
								<a href="about.html">About</a>
							</li>
							<li>
								<a href="about.html">About</a>
							</li>
							<li>
								<a href="about.html">About</a>
							</li>
							<li>
								<a href="about.html">About</a>
							</li>
						</ul>
					</div>
					<div id="tools">
						<h2>Tools</h2>
						<ul>
							<li>
								<a href="about.html">About</a>
							</li>
							<li>
								<a href="about.html">About</a>
							</li>
							<li>
								<a href="about.html">About</a>
							</li>
							<li>
								<a href="about.html">About</a>
							</li>
						</ul>
					</div>
					<div id="extras">
						<h2>Extras</h2>
						<ul>
							<li>
								<a href="about.html">About</a>
							</li>
							<li>
								<a href="about.html">About</a>
							</li>
							<li>
								<a href="about.html">About</a>
							</li>
							<li>
								<a href="about.html">About</a>
							</li>
						</ul>
					</div>
					<div id="documentation">
						<h2>Documentation</h2>
						<ul>
							<li>
								<a href="about.html">About</a>
							</li>
							<li>
								<a href="about.html">About</a>
							</li>
							<li>
								<a href="about.html">About</a>
							</li>
							<li>
								<a href="about.html">About</a>
							</li>
						</ul>
					</div>
				</div>
			</body>
		</html>
	</xsl:template>

</xsl:stylesheet>