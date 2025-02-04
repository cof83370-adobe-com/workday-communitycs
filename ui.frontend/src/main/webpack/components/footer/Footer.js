import React from 'react';
import {
	Box,
	Container,
	Row,
	Column,
	Heading,
} from './FooterStyles';

function FooterComponent(props) {
	return (
		<Box>
			<Container>
				<Row>
					<Column>
						<Heading>{props.footerItem1}</Heading>
					</Column>
					<Column>
						<Heading>{props.footerItem2}</Heading>
					</Column>
					<Column>
						<Heading>{props.footerItem3}</Heading>
					</Column>
					<Column>
					<Heading>{props.footerItem4}</Heading>
					</Column>
				</Row>
			</Container>
		</Box>

	);
}
export default FooterComponent;
